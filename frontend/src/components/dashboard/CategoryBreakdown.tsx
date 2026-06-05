import { EmptyState } from "@/components/ui/EmptyState";
import type { CategoryBreakdownItem, Tone } from "@/types/presentation";

type CategoryBreakdownProps = {
  categories: CategoryBreakdownItem[];
  periodLabel: string;
};

const barClasses: Record<Tone, string> = {
  income: "bg-secondary",
  expense: "bg-muted-coral",
  transfer: "bg-primary-container",
  amber: "bg-[#d29437]",
  neutral: "bg-primary",
};

export function CategoryBreakdown({ categories, periodLabel }: CategoryBreakdownProps) {
  return (
    <section className="min-w-0 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
      <div>
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{periodLabel}</p>
        <h2 className="mt-2 text-xl font-semibold text-primary">Categorías principales</h2>
        <p className="mt-2 text-sm leading-6 text-text-secondary">Gasto real del mes. Excluye transferencias entre cuentas.</p>
      </div>
      {categories.length > 0 ? (
        <div className="mt-6 grid gap-5">
          {categories.map((category) => (
            <div key={category.name} className="grid gap-2">
              <div className="flex items-center justify-between gap-4">
                <span className="font-medium text-primary">{category.name}</span>
                <span className="font-semibold text-primary">{category.amount}</span>
              </div>
              <div className="h-2 rounded-full bg-muted-surface" aria-hidden="true">
                <div className={`h-2 rounded-full ${barClasses[category.tone]}`} style={{ width: `${category.percent}%` }} />
              </div>
              <p className="text-xs font-medium text-text-muted">{category.percent}% del mayor gasto mensual</p>
            </div>
          ))}
        </div>
      ) : (
        <div className="mt-5">
          <EmptyState
            title="Aún no hay gastos categorizados este mes."
            description="Cuando tengas gastos con categoría, aparecerán aquí ordenados por impacto."
          />
        </div>
      )}
    </section>
  );
}
