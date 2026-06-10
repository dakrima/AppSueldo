import { CalendarDays } from "lucide-react";
import type { SalaryRhythmViewModel } from "@/types/presentation";

type SalaryRhythmCardProps = {
  rhythm: SalaryRhythmViewModel;
};

export function SalaryRhythmCard({ rhythm }: SalaryRhythmCardProps) {
  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
      <div className="flex gap-3">
        <span className="mt-0.5 grid size-10 shrink-0 place-items-center rounded-lg bg-muted-surface text-primary">
          <CalendarDays size={19} aria-hidden="true" />
        </span>
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-primary/60">Uso del sueldo</p>
          <h2 className="mt-2 text-xl font-semibold text-primary">{rhythm.title}</h2>
          <p className="mt-2 text-sm leading-6 text-primary/78">{rhythm.description}</p>
          <p className="mt-1 text-xs font-medium text-primary/60">{rhythm.note}</p>
        </div>
      </div>

      <dl className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {rhythm.facts.map((fact) => (
          <div key={fact.label} className="rounded-lg border border-border-soft bg-white/45 px-3 py-3">
            <dt className="text-xs font-bold uppercase tracking-[0.1em] text-primary/55">{fact.label}</dt>
            <dd className="mt-1 text-sm font-semibold text-primary">{fact.value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
