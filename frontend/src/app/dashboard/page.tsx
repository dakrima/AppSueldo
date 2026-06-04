import Link from "next/link";
import { Plus } from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { CategoryBreakdown } from "@/components/dashboard/CategoryBreakdown";
import { InsightCard } from "@/components/dashboard/InsightCard";
import { MainBalanceCard } from "@/components/dashboard/MainBalanceCard";
import { SummaryCard } from "@/components/dashboard/SummaryCard";
import { TransactionList } from "@/components/transactions/TransactionList";
import { Button } from "@/components/ui/Button";
import {
  getDashboardInsights,
  getDashboardSummaryCards,
} from "@/features/dashboard/data";
import { getRecentTransactionsData } from "@/features/transactions/data";

export default function DashboardPage() {
  const insights = getDashboardInsights();
  const summaryCards = getDashboardSummaryCards();
  const recentTransactions = getRecentTransactionsData();

  return (
    <AppShell
      eyebrow="Dashboard"
      title="Hola, David"
      description="Resumen de junio con movimientos ingresados manualmente."
      headerVariant="compact"
      action={
        <Button asChild>
          <Link href="/transactions/new">
            <Plus size={20} />
            Agregar movimiento
          </Link>
        </Button>
      }
    >
      <div className="grid gap-6 xl:grid-cols-[1fr_360px]">
        <div className="grid gap-6">
          <MainBalanceCard />
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {summaryCards.map((item) => (
              <SummaryCard key={item.label} {...item} />
            ))}
          </div>
          <TransactionList title="Últimos movimientos" transactions={recentTransactions} showViewAll />
        </div>
        <div className="grid content-start gap-6">
          <CategoryBreakdown />
          <InsightCard title={insights[1].title} description={insights[1].description} tone={insights[1].tone} />
        </div>
      </div>
    </AppShell>
  );
}
