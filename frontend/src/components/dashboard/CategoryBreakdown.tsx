import { categoryBreakdown } from "@/lib/mock-data";

const barClasses = {
  income: "bg-secondary",
  expense: "bg-muted-coral",
  transfer: "bg-primary-container",
  amber: "bg-[#d29437]",
  neutral: "bg-primary",
};

export function CategoryBreakdown() {
  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-6 shadow-[var(--shadow-paper)]">
      <h2 className="text-xl font-semibold text-primary">Gasto por categoría</h2>
      <div className="mt-6 grid gap-5">
        {categoryBreakdown.map((category) => (
          <div key={category.name} className="grid gap-2">
            <div className="flex items-center justify-between gap-4">
              <span className="font-medium text-primary">{category.name}</span>
              <span className="font-semibold text-primary">{category.amount}</span>
            </div>
            <div className="h-2 rounded-full bg-muted-surface">
              <div className={`h-2 rounded-full ${barClasses[category.tone]}`} style={{ width: `${category.percent}%` }} />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
