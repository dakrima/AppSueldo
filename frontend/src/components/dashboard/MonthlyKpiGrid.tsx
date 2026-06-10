import type { SummaryCardModel, Tone } from "@/types/presentation";

type MonthlyKpiGridProps = {
  items: SummaryCardModel[];
};

const iconClasses: Record<Tone, string> = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-primary",
};

const amountClasses: Record<Tone, string> = {
  income: "text-secondary",
  expense: "text-muted-coral",
  transfer: "text-primary-container",
  amber: "text-[#7a4b00]",
  neutral: "text-primary",
};

export function MonthlyKpiGrid({ items }: MonthlyKpiGridProps) {
  return (
    <section className="grid min-w-0 gap-3 sm:grid-cols-2 xl:grid-cols-4" aria-label="KPIs del mes">
      {items.map((item) => {
        const Icon = item.icon;

        return (
          <article key={item.label} className="min-w-0 rounded-xl border border-border-soft bg-soft-card p-4 shadow-[var(--shadow-paper)]">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{item.label}</p>
                <p className={`mt-3 text-2xl font-semibold tracking-normal ${amountClasses[item.tone]}`}>{item.value}</p>
              </div>
              <span className={`grid size-9 shrink-0 place-items-center rounded-lg ${iconClasses[item.tone]}`}>
                <Icon size={17} aria-hidden="true" />
              </span>
            </div>
            {item.helper ? <p className="mt-3 text-sm leading-5 text-primary">{item.helper}</p> : null}
            {item.context ? <p className="mt-1 text-xs font-medium text-text-muted">{item.context}</p> : null}
          </article>
        );
      })}
    </section>
  );
}
