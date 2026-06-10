import { EmptyState } from "@/components/ui/EmptyState";
import type { CategoryReadinessViewModel } from "@/types/presentation";

type TopExpenseCategoriesPanelProps = {
  categoryReadiness: CategoryReadinessViewModel;
  periodLabel: string;
};

export function TopExpenseCategoriesPanel({ categoryReadiness, periodLabel }: TopExpenseCategoriesPanelProps) {
  const categories = categoryReadiness.items;
  const needsCategories = categoryReadiness.status === "needs_categories";

  return (
    <section className="min-w-0 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{periodLabel}</p>
          <h2 className="mt-2 text-xl font-semibold text-primary">{categoryReadiness.title}</h2>
        </div>
        <p className="max-w-sm text-sm leading-6 text-text-secondary">{categoryReadiness.description}</p>
      </div>

      {needsCategories ? (
        <div className="mt-5 rounded-xl border border-dashed border-border-soft bg-white/45 px-4 py-4 text-primary">
          <p className="text-sm font-semibold">Aún faltan movimientos por ordenar</p>
          <p className="mt-2 text-sm leading-6 text-primary/70">
            {categoryReadiness.uncategorizedExpenseCount > 0
              ? `${categoryReadiness.uncategorizedExpenseCount} gastos están sin categoría${
                  categoryReadiness.uncategorizedPercentLabel ? ` (${categoryReadiness.uncategorizedPercentLabel} del gasto)` : ""
                }. Cuando los ordenes, este análisis mostrará mejor dónde se concentra tu gasto.`
              : "Todavía no hay categorías suficientes para construir una lectura confiable."}
          </p>
        </div>
      ) : categories.length > 0 ? (
        <div className="mt-6 grid gap-4">
          {categories.map((category, index) => (
            <article key={category.id} className="grid gap-2">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="truncate font-semibold text-primary">
                    {index + 1}. {category.name}
                  </p>
                  <p className="mt-1 text-xs font-medium text-text-muted">
                    {category.movementCount} {category.movementCount === 1 ? "movimiento registrado" : "movimientos registrados"}
                  </p>
                </div>
                <div className="shrink-0 text-right">
                  <p className="font-semibold text-primary">{category.amount}</p>
                  <p className="mt-1 text-xs font-semibold text-text-muted">{category.percentLabel} del gasto</p>
                </div>
              </div>
              <div className="h-2 rounded-full bg-muted-surface" aria-label={`${category.percentLabel} del gasto mensual`}>
                <div className="h-2 rounded-full bg-muted-coral" style={{ width: `${category.percent}%` }} />
              </div>
            </article>
          ))}
        </div>
      ) : (
        <div className="mt-5">
          <EmptyState
            title="Aún no hay gastos del mes."
            description="Cuando registres gastos, AppSueldo podrá mostrar dónde se está yendo la plata."
          />
        </div>
      )}
    </section>
  );
}
