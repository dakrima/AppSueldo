import { Transaction, recentTransactions } from "@/lib/mock-data";
import { TransactionItem } from "@/components/transactions/TransactionItem";
import { EmptyState } from "@/components/ui/EmptyState";

type TransactionListProps = {
  title: string;
  transactions?: Transaction[];
};

export function TransactionList({ title, transactions = recentTransactions }: TransactionListProps) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="font-semibold text-slate-950">{title}</h2>
          <p className="mt-1 text-sm text-slate-500">Datos mock hasta conectar los endpoints.</p>
        </div>
      </div>
      {transactions.length > 0 ? (
        <ul className="mt-2">
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
