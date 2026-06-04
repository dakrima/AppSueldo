import Link from "next/link";
import { ChevronDown, Plus, Search } from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { PeriodSummaryCard } from "@/components/transactions/PeriodSummaryCard";
import { TransactionList } from "@/components/transactions/TransactionList";
import { Button } from "@/components/ui/Button";
import { getTransactionsData } from "@/features/transactions/data";

const filters = ["Tipo: Todos", "Categoría: Todas", "Mes: Actual"];

export default function TransactionsPage() {
  const transactions = getTransactionsData();

  return (
    <AppShell
      title="Movimientos"
      description="Revisa y gestiona tus ingresos y gastos."
      action={
        <Button asChild>
          <Link href="/transactions/new">
            <Plus size={20} />
            Agregar movimiento
          </Link>
        </Button>
      }
    >
      <section className="grid gap-3 lg:grid-cols-[1fr_180px_210px_180px]">
        <label className="flex h-14 items-center gap-3 rounded-lg border border-border-soft bg-soft-card px-4 shadow-[var(--shadow-paper)]">
          <Search className="text-text-muted" size={22} />
          <input
            className="min-w-0 flex-1 bg-transparent text-base text-primary outline-none placeholder:text-text-muted"
            placeholder="Buscar movimientos..."
          />
        </label>
        {filters.map((filter) => (
          <button
            key={filter}
            className="flex h-14 items-center justify-between rounded-lg border border-border-soft bg-soft-card px-4 text-left text-base text-primary shadow-[var(--shadow-paper)]"
          >
            {filter}
            <ChevronDown className="text-text-secondary" size={19} />
          </button>
        ))}
      </section>

      <div className="grid gap-6 xl:grid-cols-[1fr_360px]">
        <TransactionList title="Historial" transactions={transactions} />
        <PeriodSummaryCard />
      </div>
    </AppShell>
  );
}
