import { ArrowRightLeft } from "lucide-react";
import type { FinancialHeroViewModel, Tone } from "@/types/presentation";

type DashboardHeroCardProps = {
  hero: FinancialHeroViewModel;
};

const toneClasses: Record<Tone, string> = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-primary",
};

export function DashboardHeroCard({ hero }: DashboardHeroCardProps) {
  return (
    <section className="min-w-0 overflow-hidden rounded-2xl border border-primary/10 bg-primary text-white shadow-[var(--shadow-paper)]">
      <div className="grid gap-6 p-5 sm:p-7 lg:grid-cols-[minmax(0,1fr)_310px] lg:p-8">
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-white/65">{hero.title}</p>
          <div className="mt-4 max-w-3xl">
            <span className={`inline-flex rounded-lg px-3 py-1.5 text-sm font-semibold ${toneClasses[hero.tone]}`}>
              {hero.headline}
            </span>
          </div>
          <p className="mt-5 text-xs font-bold uppercase tracking-[0.12em] text-white/65">{hero.primaryLabel}</p>
          <p className="mt-3 text-5xl font-semibold tracking-normal text-white sm:text-6xl lg:text-7xl">{hero.primaryValue}</p>
          <p className="mt-4 max-w-xl text-base leading-7 text-white/75">{hero.primaryHelper}</p>
        </div>

        <aside className="self-start rounded-xl border border-white/15 bg-white/10 p-4">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-white/65">{hero.monthlyLabel}</p>
          <p className="mt-3 text-2xl font-semibold tracking-normal text-white sm:text-3xl">{hero.monthlyValue}</p>
          <p className="mt-3 text-sm leading-6 text-white/72">{hero.monthlyHelper}</p>
          <div className="mt-5 border-t border-white/12 pt-4">
            <p className="text-sm leading-6 text-white/72">{hero.explanation}</p>
          </div>
        </aside>
      </div>

      {hero.transferNote ? (
        <div className="border-t border-white/12 px-5 py-4 sm:px-7 lg:px-8">
          <div className="flex gap-3 text-sm leading-6 text-white/72">
            <ArrowRightLeft className="mt-0.5 shrink-0 text-white/70" size={18} aria-hidden="true" />
            <p>{hero.transferNote}</p>
          </div>
        </div>
      ) : null}
    </section>
  );
}
