import {
  ChevronDown,
  Download,
  Pencil,
  ShieldCheck,
  SlidersHorizontal,
  UserRound,
} from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { LogoutButton } from "@/components/auth/LogoutButton";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";

export default function SettingsPage() {
  return (
    <AppShell
      eyebrow="Cuenta"
      title="Ajustes"
      description="Revisa tu cuenta, privacidad y preferencias preparadas para el MVP."
      headerVariant="compact"
    >
      <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
        <div className="grid gap-6">
          <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)] sm:p-8">
            <h2 className="flex items-center gap-3 text-3xl font-semibold">
              <UserRound size={27} />
              Perfil
            </h2>
            <div className="mt-8 flex flex-col gap-5 border-b border-border-soft pb-8 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-4">
                <div className="grid size-16 place-items-center rounded-full border-2 border-primary bg-mint-bg text-xl font-semibold text-secondary">
                  D
                </div>
                <div>
                  <p className="text-xl font-semibold">David</p>
                  <p className="text-lg text-text-secondary">david@ejemplo.com</p>
                </div>
              </div>
              <Button variant="secondary" disabled title="Editar perfil estará disponible en una próxima etapa">
                <Pencil size={18} />
                Editar perfil
              </Button>
            </div>
            <div className="flex flex-col gap-4 border-b border-border-soft py-8 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="flex items-center gap-3 text-xl font-semibold">
                  <span className="font-bold text-[#4285f4]">G</span>
                  Cuenta de Google
                </p>
                <p className="mt-2 text-text-secondary">Conectado. Usado para inicio de sesión.</p>
              </div>
              <Badge tone="income">Conectado</Badge>
            </div>
            <div className="mt-8">
              <LogoutButton variant="button" />
            </div>
          </section>

          <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)] sm:p-8">
            <h2 className="flex items-center gap-3 text-3xl font-semibold">
              <SlidersHorizontal size={27} />
              Preferencias
            </h2>
            {[
              ["Idioma", "Idioma de la interfaz", "Español"],
              ["Moneda principal", "Moneda usada para reportes y saldos", "CLP - Peso Chileno"],
            ].map(([label, helper, value]) => (
              <div key={label} className="mt-7 flex flex-col gap-3 border-t border-border-soft pt-7 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-lg font-semibold">{label}</p>
                  <p className="mt-1 text-text-secondary">{helper}</p>
                </div>
                <button
                  className="flex h-12 min-w-48 cursor-not-allowed items-center justify-between rounded-lg border border-border-soft bg-soft-card px-4 text-base opacity-75 shadow-[var(--shadow-paper)]"
                  disabled
                  title={`${label} preparado para una próxima etapa`}
                >
                  <span className="grid text-left">
                    <span>{value}</span>
                    <span className="text-xs font-semibold text-text-muted">Preparado</span>
                  </span>
                  <ChevronDown className="text-text-muted" size={19} />
                </button>
              </div>
            ))}
          </section>
        </div>

        <aside className="grid content-start gap-6">
          <section className="rounded-2xl border border-cyan-200 bg-soft-blue-bg p-6 shadow-[var(--shadow-paper)]">
            <h2 className="flex items-center gap-3 text-3xl font-semibold">
              <ShieldCheck size={28} />
              Privacidad
            </h2>
            <div className="mt-8 rounded-lg border border-green-300 bg-mint-bg p-5">
              <p className="text-xl font-semibold text-secondary">Tu tranquilidad es lo primero</p>
              <p className="mt-4 text-lg leading-8 text-primary">
                En este MVP <strong>no conectamos bancos ni almacenamos credenciales bancarias.</strong> Tus datos
                financieros son ingresados manualmente, locales y completamente privados.
              </p>
            </div>
            <div className="mt-8">
              <h3 className="text-xl font-semibold">Exportar datos</h3>
              <p className="mt-3 text-base leading-7 text-text-secondary">
                Descarga una copia de todos tus movimientos para tu propio respaldo o análisis en hojas de cálculo.
              </p>
              <Button
                className="mt-6 w-full"
                size="lg"
                disabled
                title="La exportación CSV se activará en una próxima etapa"
              >
                <Download size={22} />
                Descargar movimientos (CSV)
              </Button>
              <div className="mt-3">
                <Badge tone="amber">Próximamente</Badge>
              </div>
            </div>
          </section>

          <section className="rounded-2xl border border-red-200 bg-soft-coral-bg p-6 text-danger shadow-[var(--shadow-paper)]">
            <h2 className="text-xl font-semibold">Zona de peligro</h2>
            <p className="mt-4 leading-7 text-primary">
              Eliminar tu cuenta borrará permanentemente todos tus registros de AppSueldo. Esta acción no se puede deshacer.
            </p>
            <button
              className="mt-5 cursor-not-allowed font-semibold underline underline-offset-4 opacity-70"
              disabled
              title="Eliminar cuenta requiere confirmación y se implementará más adelante"
            >
              Eliminar mi cuenta
            </button>
          </section>
        </aside>
      </div>
    </AppShell>
  );
}
