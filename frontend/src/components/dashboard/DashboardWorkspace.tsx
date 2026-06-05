"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AlertCircle, Plus } from "lucide-react";
import { BalanceOverviewCard } from "@/components/dashboard/BalanceOverviewCard";
import { CategoryBreakdown } from "@/components/dashboard/CategoryBreakdown";
import { CategoryDistributionTable } from "@/components/dashboard/CategoryDistributionTable";
import { InsightCard } from "@/components/dashboard/InsightCard";
import { SummaryCard } from "@/components/dashboard/SummaryCard";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { listBankConnections } from "@/features/bank-connections/api";
import { listCategories } from "@/features/categories/api";
import { getMonthlySummary } from "@/features/dashboard/api";
import {
  buildCategoryBreakdownData,
  buildDashboardInsight,
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
  const greeting = user?.name?.trim() ? `Hola, ${user.name.trim()}! 👋🏼` : "Hola! 👋🏼";
  const todayLabel = formatToday();
  const periodLabel = formatCurrentPeriod();

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
    <div className="grid min-w-0 gap-6">
      <section className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold tracking-normal text-primary sm:text-3xl">{greeting}</h1>
          <p className="mt-2 text-sm leading-6 text-text-secondary sm:text-base">
            Este es tu resumen al {todayLabel}.
          </p>
        </div>
        <Button asChild variant="secondary" className="w-full sm:w-auto">
          <Link href="/transactions/new">
            <Plus size={18} aria-hidden="true" />
            Agregar movimiento
          </Link>
        </Button>
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
      <EmptyState
        title="Aún no hay información financiera para mostrar."
        description="Conecta una cuenta o registra movimientos para ver tu resumen."
      />
    );
  }

  const balance = calculateAccountTotal(data.bankConnections, data.monthlySummary, data.transactions);
  const summaryCards = buildSummaryCards(data.monthlySummary);
  const categoryDistribution = calculateCategoryDistribution(data.transactions, data.categories);
  const categoryBreakdown = buildCategoryBreakdownData(categoryDistribution);
  const insight = buildDashboardInsight(categoryDistribution, data.monthlySummary);

  return (
    <>
      <section className="grid min-w-0 gap-4 xl:grid-cols-[minmax(0,1fr)_340px]">
        <BalanceOverviewCard balance={balance} />
        <div className="grid min-w-0 gap-3 sm:grid-cols-3 xl:grid-cols-1">
          {summaryCards.map((item) => (
            <SummaryCard key={item.label} {...item} />
          ))}
        </div>
      </section>

      <section className="grid min-w-0 gap-6 xl:grid-cols-[minmax(0,1fr)_340px]">
        <CategoryDistributionTable items={categoryDistribution} />
        <div className="grid min-w-0 content-start gap-6">
          <CategoryBreakdown categories={categoryBreakdown} periodLabel={periodLabel} />
          <InsightCard title={insight.title} description={insight.description} tone={insight.tone} />
        </div>
      </section>
    </>
  );
}

function DashboardLoadingState() {
  return (
    <div className="grid min-w-0 gap-6" aria-busy="true" aria-live="polite">
      <section className="grid min-w-0 gap-4 xl:grid-cols-[minmax(0,1fr)_340px]">
        <div className="min-h-[27rem] rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7 lg:p-8">
          <div className="h-4 w-40 rounded-full bg-muted-surface" />
          <div className="mt-5 h-12 w-64 max-w-full rounded-full bg-muted-surface" />
          <div className="mt-4 h-4 w-full max-w-md rounded-full bg-muted-surface" />
          <div className="mt-8 grid gap-3">
            {[0, 1, 2].map((item) => (
              <div key={item} className="flex items-center justify-between rounded-xl border border-border-soft px-4 py-4">
                <div className="flex items-center gap-3">
                  <div className="size-10 rounded-lg bg-muted-surface" />
                  <div>
                    <div className="h-4 w-36 rounded-full bg-muted-surface" />
                    <div className="mt-2 h-3 w-24 rounded-full bg-muted-surface" />
                  </div>
                </div>
                <div className="h-5 w-24 rounded-full bg-muted-surface" />
              </div>
            ))}
          </div>
        </div>
        <div className="grid min-w-0 gap-3 sm:grid-cols-3 xl:grid-cols-1">
          {[0, 1, 2].map((item) => (
            <div
              key={item}
              className="min-h-32 rounded-xl border border-border-soft bg-soft-card p-4 shadow-[var(--shadow-paper)] sm:p-5"
            >
              <div className="h-3 w-32 rounded-full bg-muted-surface" />
              <div className="mt-5 h-7 w-28 rounded-full bg-muted-surface" />
              <div className="mt-4 h-3 w-40 max-w-full rounded-full bg-muted-surface" />
            </div>
          ))}
        </div>
      </section>

      <section className="grid min-w-0 gap-6 xl:grid-cols-[minmax(0,1fr)_340px]">
        <div className="min-h-80 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7">
          <div className="h-5 w-56 rounded-full bg-muted-surface" />
          <div className="mt-3 h-4 w-full max-w-lg rounded-full bg-muted-surface" />
          <div className="mt-7 grid gap-3">
            {[0, 1, 2, 3].map((item) => (
              <div key={item} className="h-14 rounded-xl bg-muted-surface/70" />
            ))}
          </div>
        </div>
        <div className="grid min-w-0 content-start gap-6">
          <div className="min-h-72 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
            <div className="h-3 w-28 rounded-full bg-muted-surface" />
            <div className="mt-4 h-5 w-48 rounded-full bg-muted-surface" />
            <div className="mt-7 grid gap-5">
              {[0, 1, 2].map((item) => (
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
          <div className="h-32 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)]">
            <div className="h-4 w-32 rounded-full bg-muted-surface" />
            <div className="mt-4 h-4 w-48 rounded-full bg-muted-surface" />
            <div className="mt-3 h-3 w-full rounded-full bg-muted-surface" />
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

function formatToday() {
  return new Intl.DateTimeFormat("es-CL", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date());
}

function formatCurrentPeriod() {
  return new Intl.DateTimeFormat("es-CL", {
    month: "long",
    year: "numeric",
  }).format(new Date());
}
