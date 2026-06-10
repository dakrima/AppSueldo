import Link from "next/link";
import { EmptyState } from "@/components/ui/EmptyState";
import type { Tone, TransactionListItem } from "@/types/presentation";

type RecentTransactionsPanelProps = {
  transactions: TransactionListItem[];
};

const iconClasses: Record<Tone, string> = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-primary",
};

const amountClasses: Record<Tone, string> = {
  income: "text-secondary",
  expense: "text-muted-coral",
  transfer: "text-primary",
  amber: "text-[#7a4b00]",
  neutral: "text-primary",
};

export function RecentTransactionsPanel({ transactions }: RecentTransactionsPanelProps) {
  const reviewCount = transactions.filter((transaction) => transaction.reviewStatus === "Sin categoría").length;
  const title =
    transactions.length > 0 && reviewCount >= Math.ceil(transactions.length / 2)
      ? "Movimientos recientes por revisar"
      : "Movimientos recientes";

  return (
    <section className="min-w-0 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">Detalle</p>
          <h2 className="mt-2 text-xl font-semibold text-primary">{title}</h2>
        </div>
        <Link href="/transactions" className="shrink-0 text-sm font-semibold text-text-secondary transition hover:text-primary">
          Ver todo
        </Link>
      </div>

      {transactions.length > 0 ? (
        <ul className="mt-5 divide-y divide-border-soft">
          {transactions.map((transaction) => {
            const Icon = transaction.icon;
            const reviewTone = transaction.reviewTone ?? transaction.tone;
            const reviewStatus = transaction.reviewStatus ?? "Registrado";
            const sourceLabel = transaction.sourceLabel ?? "Registro manual";

            return (
              <li key={transaction.id} className="flex min-w-0 items-start justify-between gap-4 py-4 first:pt-0 last:pb-0">
                <div className="flex min-w-0 gap-3">
                  <span className={`grid size-10 shrink-0 place-items-center rounded-lg ${iconClasses[transaction.tone]}`}>
                    <Icon size={18} aria-hidden="true" />
                  </span>
                  <div className="min-w-0">
                    <div className="flex min-w-0 flex-wrap items-center gap-2">
                      <p className="min-w-0 truncate text-sm font-semibold text-primary">{transaction.description}</p>
                      <span className={`rounded-md px-2 py-0.5 text-xs font-bold ${iconClasses[transaction.tone]}`}>
                        {transaction.typeLabel}
                      </span>
                      <span className={`rounded-md px-2 py-0.5 text-xs font-bold ${iconClasses[reviewTone]}`}>
                        {reviewStatus}
                      </span>
                    </div>
                    <p className="mt-1 text-sm leading-5 text-text-secondary">
                      {transaction.date} · {transaction.category} · {transaction.bankAccountName}
                    </p>
                    <p className="mt-1 text-xs font-medium text-text-muted">
                      Origen: {sourceLabel}
                    </p>
                  </div>
                </div>
                <p className={`shrink-0 text-right text-sm font-semibold ${amountClasses[transaction.tone]}`}>
                  {transaction.amount}
                </p>
              </li>
            );
          })}
        </ul>
      ) : (
        <div className="mt-5">
          <EmptyState
            title="Sin movimientos recientes."
            description="Agrega un movimiento para construir el historial financiero del mes."
          />
        </div>
      )}
    </section>
  );
}
