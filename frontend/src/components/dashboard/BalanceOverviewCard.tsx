import { ArrowRightLeft, WalletCards } from "lucide-react";
import { EmptyState } from "@/components/ui/EmptyState";
import type { BalanceOverviewViewModel } from "@/types/presentation";

type BalanceOverviewCardProps = {
  balance: BalanceOverviewViewModel;
};

export function BalanceOverviewCard({ balance }: BalanceOverviewCardProps) {
  const hasAccounts = balance.accounts.length > 0;
  const totalLabel = balance.accounts.length > 1 ? "Total consolidado" : "Saldo de la cuenta";

  if (!hasAccounts) {
    return (
      <EmptyState
        title="Aún no tienes cuentas registradas"
        description="Puedes comenzar agregando movimientos manuales."
      />
    );
  }

  return (
    <section className="min-w-0 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7 lg:p-8">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{balance.label}</p>
          <p className="mt-3 text-4xl font-semibold tracking-normal text-primary sm:text-5xl">{balance.total}</p>
          <p className="mt-3 max-w-xl text-sm leading-6 text-text-secondary sm:text-base">{balance.helper}</p>
        </div>

        <div className="rounded-xl border border-border-soft bg-warm-canvas/60 p-4 lg:min-w-64">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{totalLabel}</p>
          <p className="mt-2 text-2xl font-semibold text-primary">{balance.total}</p>
          <p className="mt-2 text-xs leading-5 text-text-secondary">{balance.updatedCopy}</p>
        </div>
      </div>

      <div className="mt-7 grid min-w-0 gap-3">
        {balance.accounts.map((account) => (
          <div
            key={account.id}
            className="flex flex-col gap-3 rounded-xl border border-border-soft bg-soft-card px-4 py-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex min-w-0 items-center gap-3">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-muted-surface text-primary">
                <WalletCards size={19} aria-hidden="true" />
              </span>
              <div className="min-w-0">
                <p className="truncate text-base font-semibold text-primary">{account.name}</p>
                <p className="mt-1 text-sm text-text-secondary">
                  {[account.institutionName, account.kind, account.currency].filter(Boolean).join(" · ")}
                </p>
              </div>
            </div>
            <p className="text-xl font-semibold tracking-normal text-primary">{account.balance}</p>
          </div>
        ))}
      </div>

      {balance.transferNote ? (
        <div className="mt-5 flex gap-3 rounded-xl border border-cyan-200 bg-soft-blue-bg px-4 py-3 text-primary-container">
          <ArrowRightLeft className="mt-0.5 shrink-0" size={18} aria-hidden="true" />
          <p className="text-sm leading-6">{balance.transferNote}</p>
        </div>
      ) : null}
    </section>
  );
}
