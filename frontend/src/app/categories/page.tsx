import { Plus } from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { CategoryCard, CreateCategoryCard } from "@/components/categories/CategoryCard";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { getCategoriesData } from "@/features/categories/data";

export default function CategoriesPage() {
  const categories = getCategoriesData();

  return (
    <AppShell
      eyebrow="Organización"
      title="Categorías"
      description="Revisa cómo se agrupan tus movimientos este mes."
      headerVariant="compact"
      action={
        <div className="grid justify-items-start gap-2 sm:justify-items-end">
          <Button disabled title="La creación de categorías se activará en una próxima etapa">
            <Plus size={20} />
            Crear categoría
          </Button>
          <Badge tone="amber">Próximamente</Badge>
        </div>
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
