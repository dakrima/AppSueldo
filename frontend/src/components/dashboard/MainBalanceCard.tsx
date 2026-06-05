import { CalendarDays } from "lucide-react";
import type { MainBalanceViewModel } from "@/types/presentation";

type MainBalanceCardProps = {
  mainBalance: MainBalanceViewModel;
};

export function MainBalanceCard({ mainBalance }: MainBalanceCardProps) {
  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)] sm:p-8 lg:p-9">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-2 text-sm font-semibold text-text-secondary">
          <CalendarDays size={18} aria-hidden="true" />
          {mainBalance.month}
        </div>
        <p className="w-fit rounded-md bg-muted-surface px-3 py-1 text-xs font-bold text-text-secondary">Registro manual</p>
      </div>
      <div className="mt-7">
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{mainBalance.label}</p>
        <p className="mt-3 text-4xl font-semibold tracking-normal text-primary sm:text-5xl">{mainBalance.value}</p>
        <p className="mt-4 max-w-2xl text-base leading-7 text-text-secondary">{mainBalance.helper}</p>
      </div>
      <dl className="mt-7 grid gap-3 border-t border-border-soft pt-5 sm:grid-cols-2">
        {mainBalance.supportingFacts.map((fact) => (
          <div key={fact.label} className="rounded-lg bg-muted-surface/60 px-4 py-3">
            <dt className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{fact.label}</dt>
            <dd className="mt-1 text-sm font-semibold text-primary">{fact.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
