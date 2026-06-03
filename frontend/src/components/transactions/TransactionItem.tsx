import { ArrowDownLeft, ArrowUpRight, Repeat } from "lucide-react";
import { Transaction } from "@/lib/mock-data";

const icons = {
  INCOME: ArrowDownLeft,
  EXPENSE: ArrowUpRight,
  TRANSFER: Repeat,
};

export function TransactionItem({ transaction }: { transaction: Transaction }) {
  const Icon = icons[transaction.type];
  const amountClass = transaction.type === "INCOME" ? "text-emerald-700" : "text-slate-950";

  return (
    <li className="flex items-center justify-between gap-4 border-b border-slate-100 py-4 last:border-0">
      <div className="flex min-w-0 items-center gap-3">
        <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
          <Icon size={18} />
        </span>
        <div className="min-w-0">
          <p className="truncate font-medium text-slate-950">{transaction.description}</p>
          <p className="mt-1 text-sm text-slate-500">
            {transaction.category} · {transaction.date}
          </p>
        </div>
      </div>
      <p className={`shrink-0 text-sm font-semibold ${amountClass}`}>{transaction.amount}</p>
    </li>
  );
}
