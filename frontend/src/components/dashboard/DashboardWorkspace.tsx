"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AlertCircle, Plus, Upload } from "lucide-react";
import { DashboardHeroCard } from "@/components/dashboard/DashboardHeroCard";
import { MonthlyKpiGrid } from "@/components/dashboard/MonthlyKpiGrid";
import { MonthlyPriorityCard } from "@/components/dashboard/MonthlyPriorityCard";
import { RecentTransactionsPanel } from "@/components/dashboard/RecentTransactionsPanel";
import { SalaryRhythmCard } from "@/components/dashboard/SalaryRhythmCard";
import { TopExpenseCategoriesPanel } from "@/components/dashboard/TopExpenseCategoriesPanel";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { listBankConnections } from "@/features/bank-connections/api";
import { listCategories } from "@/features/categories/api";
import { getMonthlySummary } from "@/features/dashboard/api";
import {
  buildCategoryReadiness,
  buildFinancialHero,
  buildMonthlyPriority,
  buildRecentTransactions,
  buildSalaryRhythm,
  buildSummaryCards,
  calculateAccountTotal,
  calculateCategoryDistribution,
  hasFinancialDashboardData,
} from "@/features/dashboard/derived";
import { listTransactions } from "@/features/transactions/api";
import { useAuth } from "@/hooks/useAuth";
import type { BankConnection, Category, MonthlySummary, Transaction } from "@/types/finance";

type DashboardData = {
  monthlySummary: MonthlySummary;
  transactions: Transaction[];
  bankConnections: BankConnection[];
  categories: Category[];
};

type DashboardState =
  | { status: "loading" }
  | { status: "error" }
  | { status: "success"; data: DashboardData };

export function DashboardWorkspace() {
  const { user } = useAuth();
  const [dashboardState, setDashboardState] = useState<DashboardState>({ status: "loading" });
  const [loadRequest, setLoadRequest] = useState(0);
  const periodLabel = formatCurrentPeriod();
  const userName = user?.name?.trim();

  useEffect(() => {
    let active = true;
    const timeoutId = window.setTimeout(() => {
      void fetchDashboardData()
        .then((data) => {
          if (!active) {
            return;
          }
          setDashboardState({ status: "success", data });
        })
        .catch(() => {
          if (!active) {
            return;
          }
          setDashboardState({ status: "error" });
        });
    }, 0);

    return () => {
      active = false;
      window.clearTimeout(timeoutId);
    };
  }, [loadRequest]);

  function handleRetry() {
    setDashboardState({ status: "loading" });
    setLoadRequest((request) => request + 1);
  }

  return (
    <div className="grid min-w-0 gap-5">
      <section className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold tracking-normal text-primary sm:text-3xl">Resumen financiero</h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-text-secondary sm:text-base">
            {userName ? `${userName}, ` : ""}este es tu control mensual para {periodLabel}.
          </p>
        </div>
        <div className="grid gap-2 sm:flex sm:items-center">
          <Button asChild className="w-full sm:w-auto">
            <Link href="/transactions/new">
              <Plus size={18} aria-hidden="true" />
              Agregar movimiento
            </Link>
          </Button>
          <Button asChild variant="secondary" className="w-full sm:w-auto">
            <Link href="/transactions">
              <Upload size={18} aria-hidden="true" />
              Importar CSV
            </Link>
          </Button>
        </div>
      </section>

      {dashboardState.status === "loading" ? <DashboardLoadingState /> : null}

      {dashboardState.status === "error" ? <DashboardErrorState onRetry={handleRetry} /> : null}

      {dashboardState.status === "success" ? (
        <DashboardContent data={dashboardState.data} periodLabel={periodLabel} />
      ) : null}
    </div>
  );
}

function DashboardContent({ data, periodLabel }: { data: DashboardData; periodLabel: string }) {
  const hasData = hasFinancialDashboardData(data.bankConnections, data.transactions);

  if (!hasData) {
    return (
      <section className="grid gap-4 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center sm:p-7">
        <EmptyState
          title="Aún no hay información financiera para mostrar."
          description="Agrega un movimiento o conecta tu banco para empezar a construir tu cabina mensual."
        />
        <div className="grid gap-2 sm:w-52">
          <Button asChild>
            <Link href="/transactions/new">
              <Plus size={18} aria-hidden="true" />
              Agregar movimiento
            </Link>
          </Button>
          <Button asChild variant="secondary">
            <Link href="/settings">Conectar banco</Link>
          </Button>
        </div>
      </section>
    );
  }

  const balance = calculateAccountTotal(data.bankConnections, data.monthlySummary, data.transactions);
  const hero = buildFinancialHero(balance, data.monthlySummary);
  const summaryCards = buildSummaryCards(data.monthlySummary, data.transactions, data.bankConnections);
  const categoryDistribution = calculateCategoryDistribution(data.transactions, data.categories);
  const categoryReadiness = buildCategoryReadiness(categoryDistribution, data.transactions);
  const monthlyPriority = buildMonthlyPriority(data.monthlySummary, data.transactions);
  const salaryRhythm = buildSalaryRhythm(data.monthlySummary);
  const recentTransactions = buildRecentTransactions(data.transactions);

  return (
    <div className="grid min-w-0 gap-5">
      <DashboardHeroCard hero={hero} />

      <MonthlyPriorityCard priority={monthlyPriority} />

      <MonthlyKpiGrid items={summaryCards} />

      <SalaryRhythmCard rhythm={salaryRhythm} />

      <section className="grid min-w-0 gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
        <RecentTransactionsPanel transactions={recentTransactions} />
        <TopExpenseCategoriesPanel categoryReadiness={categoryReadiness} periodLabel={periodLabel} />
      </section>
    </div>
  );
}

function DashboardLoadingState() {
  return (
    <div className="grid min-w-0 gap-5" aria-busy="true" aria-live="polite">
      <section className="min-h-80 rounded-2xl border border-primary/10 bg-primary p-5 shadow-[var(--shadow-paper)] sm:p-7 lg:p-8">
        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_310px]">
          <div>
            <div className="h-4 w-44 rounded-full bg-white/15" />
            <div className="mt-5 h-9 w-96 max-w-full rounded-lg bg-white/15" />
            <div className="mt-6 h-16 w-72 max-w-full rounded-full bg-white/15" />
            <div className="mt-5 h-4 w-full max-w-xl rounded-full bg-white/15" />
          </div>
          <div className="rounded-xl border border-white/15 bg-white/10 p-4">
            <div className="h-4 w-36 rounded-full bg-white/15" />
            <div className="mt-5 h-9 w-40 rounded-full bg-white/15" />
            <div className="mt-4 h-4 w-full rounded-full bg-white/15" />
          </div>
        </div>
      </section>

      <div className="min-h-52 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
        <div className="h-3 w-36 rounded-full bg-muted-surface" />
        <div className="mt-4 h-6 w-56 rounded-full bg-muted-surface" />
        <div className="mt-4 h-4 w-full max-w-2xl rounded-full bg-muted-surface" />
        <div className="mt-6 grid gap-3 sm:grid-cols-3">
          {[0, 1, 2].map((item) => (
            <div key={item} className="h-20 rounded-xl bg-muted-surface/70" />
          ))}
        </div>
      </div>

      <div className="grid min-w-0 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[0, 1, 2, 3].map((item) => (
          <div key={item} className="min-h-32 rounded-xl border border-border-soft bg-soft-card p-4 shadow-[var(--shadow-paper)]">
            <div className="h-3 w-32 rounded-full bg-muted-surface" />
            <div className="mt-5 h-7 w-28 rounded-full bg-muted-surface" />
            <div className="mt-4 h-3 w-40 max-w-full rounded-full bg-muted-surface" />
          </div>
        ))}
      </div>

      <div className="min-h-44 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
        <div className="h-3 w-32 rounded-full bg-muted-surface" />
        <div className="mt-4 h-6 w-44 rounded-full bg-muted-surface" />
        <div className="mt-5 grid gap-3 sm:grid-cols-3">
          {[0, 1, 2].map((item) => (
            <div key={item} className="h-16 rounded-xl bg-muted-surface/70" />
          ))}
        </div>
      </div>

      <section className="grid min-w-0 gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <div className="min-h-80 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
          <div className="h-3 w-28 rounded-full bg-muted-surface" />
          <div className="mt-4 h-6 w-56 rounded-full bg-muted-surface" />
          <div className="mt-7 grid gap-5">
            {[0, 1, 2, 3].map((item) => (
              <div key={item} className="grid gap-2">
                <div className="flex justify-between">
                  <div className="h-4 w-28 rounded-full bg-muted-surface" />
                  <div className="h-4 w-20 rounded-full bg-muted-surface" />
                </div>
                <div className="h-2 rounded-full bg-muted-surface" />
              </div>
            ))}
          </div>
        </div>
        <div className="min-h-80 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
          <div className="h-3 w-24 rounded-full bg-muted-surface" />
          <div className="mt-4 h-6 w-52 rounded-full bg-muted-surface" />
          <div className="mt-6 grid gap-3">
            {[0, 1, 2, 3].map((item) => (
              <div key={item} className="h-14 rounded-xl bg-muted-surface/70" />
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

function DashboardErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <section
      className="rounded-2xl border border-red-200 bg-soft-coral-bg p-6 text-danger shadow-[var(--shadow-paper)]"
      role="alert"
    >
      <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex gap-3">
          <AlertCircle className="mt-1 shrink-0" size={22} aria-hidden="true" />
          <div>
            <h2 className="text-lg font-semibold">No pudimos cargar tu resumen financiero.</h2>
            <p className="mt-2 max-w-2xl text-sm leading-6">
              Revisa que el backend esté corriendo o vuelve a intentarlo.
            </p>
          </div>
        </div>
        <Button type="button" variant="secondary" onClick={onRetry} className="w-full sm:w-auto">
          Reintentar
        </Button>
      </div>
    </section>
  );
}

async function fetchDashboardData(): Promise<DashboardData> {
  const [monthlySummary, transactions, bankConnections, categories] = await Promise.all([
    getMonthlySummary(),
    listTransactions(),
    listBankConnections(),
    listCategories(),
  ]);

  return {
    monthlySummary,
    transactions,
    bankConnections,
    categories,
  };
}

function formatCurrentPeriod() {
  return new Intl.DateTimeFormat("es-CL", {
    month: "long",
    year: "numeric",
  }).format(new Date());
}
