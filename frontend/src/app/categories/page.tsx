import { Plus } from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { CategoryCard, CreateCategoryCard } from "@/components/categories/CategoryCard";
import { Button } from "@/components/ui/Button";
import { categories } from "@/lib/mock-data";

export default function CategoriesPage() {
  return (
    <AppShell
      title="Categorías"
      description="Agrupa tus movimientos para entender dónde se va tu sueldo."
      action={
        <Button>
          <Plus size={20} />
          Crear categoría
        </Button>
      }
    >
      <section className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
        {categories.map((category) => (
          <CategoryCard key={category.id} category={category} />
        ))}
        <CreateCategoryCard />
      </section>
    </AppShell>
  );
}
