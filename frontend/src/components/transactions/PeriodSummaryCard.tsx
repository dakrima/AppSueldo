import { ArrowDown, ArrowUp } from "lucide-react";
import { getPeriodSummaryData } from "@/features/transactions/data";

export function PeriodSummaryCard() {
  const periodSummary = getPeriodSummaryData();

  return (
    <aside className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)]">
      <h2 className="text-2xl font-semibold text-primary">Resumen del periodo</h2>
      <div className="mt-6 grid gap-6">
        <div>
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.12em] text-text-secondary">
            <ArrowUp className="text-secondary" size={16} />
            Total ingresos
          </div>
          <p className="mt-2 text-4xl font-semibold tracking-normal text-secondary">{periodSummary.income}</p>
        </div>
        <div className="border-t border-border-soft pt-6">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.12em] text-text-secondary">
            <ArrowDown className="text-muted-coral" size={16} />
            Total gastos
          </div>
          <p className="mt-2 text-4xl font-semibold tracking-normal text-muted-coral">{periodSummary.expenses}</p>
          <p className="mt-3 text-base leading-6 text-text-secondary">{periodSummary.note}</p>
        </div>
      </div>
    </aside>
  );
}
