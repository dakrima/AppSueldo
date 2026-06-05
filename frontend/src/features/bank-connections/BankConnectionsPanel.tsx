"use client";

import { useCallback, useMemo, useRef, useState, useEffect } from "react";
import { Building2, Landmark, PlugZap, RefreshCw, ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorMessage } from "@/components/ui/ErrorMessage";
import { LoadingState } from "@/components/ui/LoadingState";
import { SuccessState } from "@/components/ui/SuccessState";
import {
  createFintocLinkIntent,
  exchangeFintocToken,
  listBankConnections,
  syncBankConnection,
} from "@/features/bank-connections/api";
import type { BankAccount, BankConnection, BankConnectionStatus, BankConnectionSyncResponse } from "@/types/finance";

type FintocSuccessPayload = {
  exchangeToken?: unknown;
  exchange_token?: unknown;
};

type FintocWidget = {
  open: () => void;
  hide?: () => void;
  destroy?: () => void;
};

type FintocCreateOptions = {
  publicKey: string;
  widgetToken: string;
  product: string;
  country: string;
  holderType: "individual";
  onSuccess: (payload: FintocSuccessPayload) => void;
  onExit: () => void;
};

declare global {
  interface Window {
    Fintoc?: {
      create: (options: FintocCreateOptions) => FintocWidget;
    };
  }
}

const FINTOC_SCRIPT_ID = "fintoc-widget-script";
const FINTOC_SCRIPT_URL = "https://js.fintoc.com/v1/";

let fintocScriptPromise: Promise<void> | null = null;

type ConnectState = "idle" | "loading-script" | "creating-intent" | "widget-open" | "exchanging" | "success" | "cancelled" | "error";

type SyncFeedback = {
  tone: "success" | "pending" | "error" | "mfa";
  message: string;
};

export function BankConnectionsPanel() {
  const [connections, setConnections] = useState<BankConnection[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [connectState, setConnectState] = useState<ConnectState>("idle");
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [syncingConnectionId, setSyncingConnectionId] = useState<number | null>(null);
  const [syncFeedbackByConnection, setSyncFeedbackByConnection] = useState<Record<number, SyncFeedback>>({});
  const widgetRef = useRef<FintocWidget | null>(null);

  const bankConnections = useMemo(
    () => connections.filter((connection) => connection.provider === "FINTOC"),
    [connections],
  );

  const loadConnections = useCallback(async () => {
    setError("");
    setIsLoading(true);
    try {
      setConnections(await listBankConnections());
    } catch (error) {
      setError(error instanceof Error ? error.message : "No pudimos cargar tus conexiones bancarias.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let mounted = true;
    queueMicrotask(() => {
      if (mounted) {
        void loadConnections();
      }
    });
    return () => {
      mounted = false;
    };
  }, [loadConnections]);

  useEffect(() => {
    return () => {
      widgetRef.current?.destroy?.();
      widgetRef.current = null;
    };
  }, []);

  async function handleConnectBank() {
    setError("");
    setSuccessMessage("");
    setConnectState("creating-intent");

    try {
      const linkIntent = await createFintocLinkIntent();
      setConnectState("loading-script");
      await loadFintocScript();

      if (!window.Fintoc) {
        throw new Error("No pudimos cargar el conector bancario. Intenta nuevamente.");
      }

      const widget = window.Fintoc.create({
        publicKey: linkIntent.publicKey,
        widgetToken: linkIntent.widgetToken,
        product: linkIntent.product,
        country: linkIntent.country,
        holderType: "individual",
        onSuccess: (payload) => {
          void handleWidgetSuccess(payload);
        },
        onExit: () => {
          widgetRef.current?.destroy?.();
          widgetRef.current = null;
          setConnectState("cancelled");
          setSuccessMessage("");
          setError("");
        },
      });

      widgetRef.current = widget;
      setConnectState("widget-open");
      widget.open();
    } catch (error) {
      widgetRef.current?.destroy?.();
      widgetRef.current = null;
      setConnectState("error");
      setError(error instanceof Error ? error.message : "No pudimos iniciar la conexión bancaria.");
    }
  }

  async function handleWidgetSuccess(payload: FintocSuccessPayload) {
    const exchangeToken = extractExchangeToken(payload);
    if (!exchangeToken) {
      widgetRef.current?.destroy?.();
      widgetRef.current = null;
      setConnectState("error");
      setError("El banco se conectó, pero no recibimos la confirmación necesaria. Intenta nuevamente.");
      return;
    }

    setConnectState("exchanging");
    setError("");
    try {
      await exchangeFintocToken(exchangeToken);
      widgetRef.current?.destroy?.();
      widgetRef.current = null;
      setSuccessMessage("Banco conectado. Ya puedes ver sus cuentas asociadas.");
      setConnectState("success");
      await loadConnections();
    } catch (error) {
      widgetRef.current?.destroy?.();
      widgetRef.current = null;
      setConnectState("error");
      setError(error instanceof Error ? error.message : "No pudimos guardar la conexión bancaria.");
    }
  }

  async function handleSyncConnection(connectionId: number) {
    setError("");
    setSuccessMessage("");
    setSyncingConnectionId(connectionId);
    setSyncFeedbackByConnection((current) => {
      const next = { ...current };
      delete next[connectionId];
      return next;
    });

    try {
      const response = await syncBankConnection(connectionId);
      setSyncFeedbackByConnection((current) => ({
        ...current,
        [connectionId]: syncFeedback(response),
      }));
      if (!response.requiresMfa) {
        await loadConnections();
      }
    } catch (error) {
      setSyncFeedbackByConnection((current) => ({
        ...current,
        [connectionId]: {
          tone: "error",
          message: error instanceof Error ? error.message : "No pudimos sincronizar esta conexión.",
        },
      }));
    } finally {
      setSyncingConnectionId(null);
    }
  }

  const isConnecting = ["loading-script", "creating-intent", "widget-open", "exchanging"].includes(connectState);

  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)] sm:p-8">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 className="flex items-center gap-3 text-3xl font-semibold">
            <Landmark size={27} />
            Conexiones bancarias
          </h2>
          <p className="mt-3 max-w-2xl text-base leading-7 text-text-secondary">
            Conecta tu banco para leer cuentas y mantener tus movimientos actualizados desde AppSueldo.
          </p>
        </div>
        <Button type="button" size="lg" onClick={handleConnectBank} disabled={isConnecting || isLoading}>
          <PlugZap size={22} />
          {isConnecting ? "Conectando..." : "Conectar banco"}
        </Button>
      </div>

      <div className="mt-5 flex flex-wrap gap-3">
        <Badge tone="income">Fintoc Movements</Badge>
        <Badge tone="amber">Solo lectura</Badge>
        <Badge tone="neutral">Sin pagos ni transferencias</Badge>
      </div>

      <div className="mt-6 grid gap-4">
        {statusMessage(connectState) ? (
          <div className="rounded-lg border border-border-soft bg-warm-canvas/60 p-4 text-sm font-medium text-text-secondary">
            {statusMessage(connectState)}
          </div>
        ) : null}

        {isConnecting ? (
          <LoadingState title="Conectando banco" description="Estamos preparando el conector seguro de Fintoc." />
        ) : null}

        {connectState === "cancelled" ? (
          <div className="rounded-lg border border-border-soft bg-warm-canvas/60 p-4 text-sm font-medium text-text-secondary" role="status">
            Conexión cancelada. Puedes intentarlo nuevamente cuando quieras.
          </div>
        ) : null}

        {successMessage ? <SuccessState title="Banco conectado" message={successMessage} /> : null}

        {error ? <ErrorMessage title="No pudimos conectar el banco" message={error} /> : null}
      </div>

      <div className="mt-8">
        {isLoading ? (
          <LoadingState title="Cargando conexiones" description="Buscando bancos conectados a tu cuenta." />
        ) : bankConnections.length === 0 ? (
          <EmptyState
            title="Aún no hay bancos conectados"
            description="Conecta tu primer banco para dejar listas sus cuentas. El MVP manual seguirá disponible."
          />
        ) : (
          <div className="grid gap-4">
            {bankConnections.map((connection) => (
              <BankConnectionCard
                key={connection.id}
                connection={connection}
                feedback={syncFeedbackByConnection[connection.id]}
                isSyncing={syncingConnectionId === connection.id}
                onSync={handleSyncConnection}
              />
            ))}
          </div>
        )}
      </div>

      <div className="mt-8 rounded-lg border border-green-300 bg-mint-bg p-5">
        <p className="flex items-center gap-2 text-lg font-semibold text-secondary">
          <ShieldCheck size={20} />
          Seguridad
        </p>
        <p className="mt-2 text-sm leading-6 text-primary">
          AppSueldo no recibe ni guarda tus credenciales bancarias. La conexión se confirma en el backend y los tokens sensibles no se muestran en pantalla.
        </p>
      </div>
    </section>
  );
}

function BankConnectionCard({
  connection,
  feedback,
  isSyncing,
  onSync,
}: {
  connection: BankConnection;
  feedback?: SyncFeedback;
  isSyncing: boolean;
  onSync: (connectionId: number) => void;
}) {
  return (
    <article className="rounded-xl border border-border-soft bg-warm-canvas/60 p-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-3">
          <span className="grid size-11 shrink-0 place-items-center rounded-lg border border-border-soft bg-soft-card text-primary">
            <Building2 size={21} />
          </span>
          <div>
            <h3 className="text-lg font-semibold text-primary">{connection.institutionName ?? "Banco conectado"}</h3>
            <p className="mt-1 text-sm text-text-secondary">
              {connection.accounts.length} {connection.accounts.length === 1 ? "cuenta asociada" : "cuentas asociadas"}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2 sm:justify-end">
          <Badge tone={connection.status === "ACTIVE" ? "income" : "amber"}>{statusLabel(connection.status)}</Badge>
          <Button
            type="button"
            variant="secondary"
            size="sm"
            onClick={() => onSync(connection.id)}
            disabled={isSyncing || connection.status !== "ACTIVE"}
            title={connection.status === "ACTIVE" ? "Sincronizar movimientos" : "La conexión debe estar activa para sincronizar"}
          >
            <RefreshCw size={16} className={isSyncing ? "animate-spin" : undefined} />
            {isSyncing ? "Sincronizando..." : "Sincronizar"}
          </Button>
        </div>
      </div>

      {feedback ? (
        <div className={`mt-4 rounded-lg border p-4 text-sm font-medium ${syncFeedbackClasses(feedback.tone)}`} role="status">
          {feedback.message}
        </div>
      ) : null}

      <div className="mt-5 grid gap-3">
        {connection.accounts.map((account) => (
          <BankAccountRow key={account.id} account={account} />
        ))}
      </div>
    </article>
  );
}

function BankAccountRow({ account }: { account: BankAccount }) {
  return (
    <div className="flex flex-col gap-2 rounded-lg border border-border-soft bg-soft-card px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="font-semibold text-primary">{account.name}</p>
        <p className="mt-1 text-sm text-text-secondary">
          {[account.accountType, account.currency].filter(Boolean).join(" · ")}
        </p>
      </div>
      <p className="text-sm font-semibold text-primary">{formatBalance(account.balance, account.currency)}</p>
    </div>
  );
}

function statusMessage(connectState: ConnectState) {
  if (connectState === "creating-intent") {
    return "Preparando conexión segura con Fintoc.";
  }
  if (connectState === "loading-script") {
    return "Cargando el conector bancario.";
  }
  if (connectState === "widget-open") {
    return "Completa la conexión en la ventana de Fintoc.";
  }
  if (connectState === "exchanging") {
    return "Confirmando conexión y guardando cuentas.";
  }
  return "";
}

function statusLabel(status: BankConnectionStatus) {
  if (status === "ACTIVE") {
    return "Conectado";
  }
  if (status === "PENDING") {
    return "Pendiente";
  }
  if (status === "ERROR") {
    return "Requiere atención";
  }
  return "Inactivo";
}

function syncFeedback(response: BankConnectionSyncResponse): SyncFeedback {
  if (response.requiresMfa) {
    return {
      tone: "mfa",
      message: "Fintoc requiere validación adicional. Esta parte se implementará después.",
    };
  }

  if (isPendingStatus(response.status)) {
    return {
      tone: "pending",
      message: "Sincronización en proceso. Se actualizará cuando Fintoc confirme.",
    };
  }

  if (isSuccessStatus(response.syncStatus) || isSuccessStatus(response.status)) {
    const imported = response.importedTransactionsCount ?? 0;
    const skipped = response.skippedTransactionsCount ?? 0;
    const detail = imported > 0
      ? ` Se importaron ${imported} ${imported === 1 ? "movimiento nuevo" : "movimientos nuevos"}.`
      : skipped > 0
        ? " No había movimientos nuevos para importar."
        : "";

    return {
      tone: "success",
      message: `Movimientos actualizados.${detail}`,
    };
  }

  if (isPendingStatus(response.syncStatus)) {
    return {
      tone: "pending",
      message: "Sincronización en proceso. Se actualizará cuando Fintoc confirme.",
    };
  }

  return {
    tone: "error",
    message: "No pudimos sincronizar esta conexión. Intenta nuevamente más tarde.",
  };
}

function isSuccessStatus(status: string | null) {
  return ["COMPLETED", "SUCCESS", "SUCCEEDED", "OK"].includes((status ?? "").toUpperCase());
}

function isPendingStatus(status: string | null) {
  return ["PENDING", "CREATED", "PROCESSING"].includes((status ?? "").toUpperCase());
}

function syncFeedbackClasses(tone: SyncFeedback["tone"]) {
  if (tone === "success") {
    return "border-green-300 bg-mint-bg text-secondary";
  }
  if (tone === "pending" || tone === "mfa") {
    return "border-amber-200 bg-amber-bg text-[#7a4b00]";
  }
  return "border-red-200 bg-soft-coral-bg text-danger";
}

function formatBalance(balance: number | null, currency: string) {
  if (balance === null) {
    return "Saldo no disponible";
  }
  return new Intl.NumberFormat("es-CL", {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(balance);
}

function extractExchangeToken(payload: FintocSuccessPayload) {
  const token = payload.exchangeToken ?? payload.exchange_token;
  return typeof token === "string" && token.trim() ? token.trim() : "";
}

function loadFintocScript() {
  if (typeof window === "undefined") {
    return Promise.reject(new Error("El conector bancario solo puede abrirse en el navegador."));
  }
  if (window.Fintoc) {
    return Promise.resolve();
  }
  if (fintocScriptPromise) {
    return fintocScriptPromise;
  }

  fintocScriptPromise = new Promise<void>((resolve, reject) => {
    const existingScript = document.getElementById(FINTOC_SCRIPT_ID) as HTMLScriptElement | null;
    const handleLoad = () => {
      if (window.Fintoc) {
        if (existingScript) {
          existingScript.dataset.loaded = "true";
        }
        resolve();
        return;
      }
      fintocScriptPromise = null;
      reject(new Error("El conector bancario no quedó disponible. Intenta nuevamente."));
    };
    const handleError = () => {
      fintocScriptPromise = null;
      reject(new Error("No pudimos cargar el conector bancario. Revisa tu conexión e intenta de nuevo."));
    };

    if (existingScript) {
      if (existingScript.dataset.loaded === "true") {
        handleLoad();
        return;
      }
      existingScript.addEventListener("load", handleLoad, { once: true });
      existingScript.addEventListener("error", handleError, { once: true });
      return;
    }

    const script = document.createElement("script");
    script.id = FINTOC_SCRIPT_ID;
    script.src = FINTOC_SCRIPT_URL;
    script.async = true;
    script.addEventListener("load", () => {
      script.dataset.loaded = "true";
      handleLoad();
    }, { once: true });
    script.addEventListener("error", handleError, { once: true });
    document.body.appendChild(script);
  });

  return fintocScriptPromise;
}
