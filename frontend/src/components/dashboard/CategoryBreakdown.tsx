import { categories } from "@/lib/mock-data";

export function CategoryBreakdown() {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="font-semibold text-slate-950">Gasto por categoria</h2>
          <p className="mt-1 text-sm text-slate-500">Mock mensual para validar la estructura.</p>
        </div>
      </div>
      <div className="mt-5 grid gap-4">
        {categories
          .filter((category) => category.type === "EXPENSE")
          .map((category) => (
            <div key={category.name} className="grid gap-2">
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium text-slate-700">{category.name}</span>
                <span className="text-slate-500">{category.percent}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-100">
                <div
                  className="h-2 rounded-full"
                  style={{ width: `${category.percent}%`, backgroundColor: category.color }}
                />
              </div>
            </div>
          ))}
      </div>
    </section>
  );
}
