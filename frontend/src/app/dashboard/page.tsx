import { AppShell } from "@/components/layout/AppShell";
import { CategoryBreakdown } from "@/components/dashboard/CategoryBreakdown";
import { SummaryCard } from "@/components/dashboard/SummaryCard";
import { TransactionList } from "@/components/transactions/TransactionList";
import { monthlySummary, recentTransactions } from "@/lib/mock-data";

export default function DashboardPage() {
  return (
    <AppShell title="Dashboard" description="Resumen mensual preparado para datos del usuario autenticado.">
      <div className="grid grid-cols-2 gap-3 sm:gap-4 xl:grid-cols-4">
        {monthlySummary.map((item) => (
          <SummaryCard key={item.label} {...item} />
        ))}
      </div>

      <div className="grid gap-5 lg:grid-cols-[1.3fr_0.9fr]">
        <TransactionList title="Ultimos movimientos" transactions={recentTransactions} />
        <CategoryBreakdown />
      </div>
    </AppShell>
  );
}
