import { CalendarDays, MoreHorizontal } from "lucide-react";
import { mainBalance } from "@/lib/mock-data";

export function MainBalanceCard() {
  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)] sm:p-8 lg:p-10">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-lg font-semibold text-text-secondary">
          <CalendarDays size={22} />
          {mainBalance.month}
        </div>
        <button aria-label="Más opciones" className="rounded-lg p-2 text-text-muted transition hover:bg-muted-surface">
          <MoreHorizontal size={22} />
        </button>
      </div>
      <div className="mt-8">
        <p className="text-lg text-text-secondary">{mainBalance.label}</p>
        <p className="mt-2 text-4xl font-semibold tracking-normal text-primary sm:text-5xl">{mainBalance.value}</p>
        <p className="mt-3 max-w-xl text-base leading-7 text-text-secondary">{mainBalance.helper}</p>
      </div>
    </section>
  );
}
