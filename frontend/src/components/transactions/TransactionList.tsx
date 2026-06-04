import Link from "next/link";
import { TransactionItem } from "@/components/transactions/TransactionItem";
import { EmptyState } from "@/components/ui/EmptyState";
import { getRecentTransactionsData } from "@/features/transactions/data";
import type { TransactionListItem } from "@/types/presentation";

type TransactionListProps = {
  title: string;
  transactions?: TransactionListItem[];
  description?: string;
  showViewAll?: boolean;
};

export function TransactionList({
  title,
  transactions = getRecentTransactionsData(),
  description,
  showViewAll = false,
}: TransactionListProps) {
  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-primary">{title}</h2>
          {description ? <p className="mt-2 text-base text-text-secondary">{description}</p> : null}
        </div>
        {showViewAll ? (
          <Link href="/transactions" className="text-sm font-semibold text-text-secondary transition hover:text-primary">
            Ver todo
          </Link>
        ) : null}
      </div>
      {transactions.length > 0 ? (
        <ul className="mt-5">
          {transactions.map((transaction) => (
            <TransactionItem key={transaction.id} transaction={transaction} />
          ))}
        </ul>
      ) : (
        <div className="mt-5">
          <EmptyState title="Sin movimientos" description="Agrega el primer movimiento manual para empezar a construir tu resumen." />
        </div>
      )}
    </section>
  );
}
