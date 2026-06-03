import { AppShell } from "@/components/layout/AppShell";
import { EmptyState } from "@/components/ui/EmptyState";
import { categories } from "@/lib/mock-data";

export default function CategoriesPage() {
  return (
    <AppShell title="Categorias" description="Categorias propias de cada usuario para ordenar movimientos.">
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {categories.map((category) => (
          <article key={category.name} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex items-center gap-3">
              <span className="size-3 rounded-full" style={{ backgroundColor: category.color }} />
              <div>
                <h2 className="font-semibold text-slate-950">{category.name}</h2>
                <p className="text-sm text-slate-500">{category.type === "EXPENSE" ? "Gasto" : "Ingreso"}</p>
              </div>
            </div>
          </article>
        ))}
      </section>
      {categories.length === 0 ? <EmptyState title="Sin categorias" description="Crea categorias para clasificar tus movimientos manuales." /> : null}
    </AppShell>
  );
}
