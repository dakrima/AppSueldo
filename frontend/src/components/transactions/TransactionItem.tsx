import type { TransactionListItem } from "@/types/presentation";

const toneClasses = {
  income: {
    icon: "bg-mint-bg text-secondary",
    amount: "text-secondary",
  },
  expense: {
    icon: "bg-soft-coral-bg text-muted-coral",
    amount: "text-muted-coral",
  },
  transfer: {
    icon: "bg-soft-blue-bg text-primary-container",
    amount: "text-primary",
  },
  amber: {
    icon: "bg-amber-bg text-[#7a4b00]",
    amount: "text-primary",
  },
  neutral: {
    icon: "bg-muted-surface text-primary",
    amount: "text-primary",
  },
};

export function TransactionItem({ transaction }: { transaction: TransactionListItem }) {
  const Icon = transaction.icon;
  const tone = toneClasses[transaction.tone];

  return (
    <li className="flex items-center justify-between gap-4 border-b border-border-soft py-5 last:border-0">
      <div className="flex min-w-0 items-center gap-4">
        <span className={`flex size-12 shrink-0 items-center justify-center rounded-lg ${tone.icon}`}>
          <Icon size={22} />
        </span>
        <div className="min-w-0">
          <div className="flex min-w-0 items-center gap-2">
            <p className="min-w-0 flex-1 truncate text-base font-semibold text-primary">{transaction.description}</p>
            {transaction.type === "TRANSFER" ? (
              <span className="shrink-0 rounded-md bg-soft-blue-bg px-2 py-0.5 text-xs font-bold text-primary-container">
                Transferencia
              </span>
            ) : null}
          </div>
          <p className="mt-1 text-sm text-text-secondary">
            {transaction.typeLabel} · {transaction.category} · {transaction.date}
          </p>
        </div>
      </div>
      <p className={`shrink-0 text-base font-semibold ${tone.amount}`}>{transaction.amount}</p>
    </li>
  );
}
