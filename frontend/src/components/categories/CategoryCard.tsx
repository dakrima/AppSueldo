import { Plus } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import type { CategoryCardModel } from "@/types/presentation";

const toneClasses = {
  income: {
    icon: "bg-mint-bg text-secondary",
    bar: "bg-secondary",
    value: "text-secondary",
    badge: "income" as const,
  },
  expense: {
    icon: "bg-muted-surface text-primary",
    bar: "bg-muted-coral",
    value: "text-primary",
    badge: "expense" as const,
  },
  transfer: {
    icon: "bg-soft-blue-bg text-primary-container",
    bar: "bg-primary-container",
    value: "text-primary",
    badge: "neutral" as const,
  },
  amber: {
    icon: "bg-amber-bg text-[#7a4b00]",
    bar: "bg-[#d29437]",
    value: "text-primary",
    badge: "expense" as const,
  },
  neutral: {
    icon: "bg-muted-surface text-text-secondary",
    bar: "bg-primary",
    value: "text-primary",
    badge: "neutral" as const,
  },
};

export function CategoryCard({ category }: { category: CategoryCardModel }) {
  const Icon = category.icon;
  const tone = toneClasses[category.tone];

  return (
    <article className="rounded-xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)]">
      <div className="flex items-start justify-between gap-4">
        <span className={`flex size-14 items-center justify-center rounded-lg ${tone.icon}`}>
          <Icon size={25} />
        </span>
        <Badge tone={tone.badge}>{category.type === "INCOME" ? "Ingreso" : "Gasto"}</Badge>
      </div>
      <h2 className="mt-7 text-xl font-semibold text-primary">{category.name}</h2>
      <p className={`mt-2 text-3xl font-semibold tracking-normal ${tone.value}`}>{category.amount}</p>
      <div className="mt-7 h-2 rounded-full bg-muted-surface">
        <div className={`h-2 rounded-full ${tone.bar}`} style={{ width: `${category.percent}%` }} />
      </div>
    </article>
  );
}

export function CreateCategoryCard() {
  return (
    <button
      className="grid min-h-60 cursor-not-allowed place-items-center rounded-xl border-2 border-dashed border-border-strong bg-transparent p-6 text-center opacity-75"
      disabled
      title="Crear categorías estará disponible en una próxima etapa"
    >
      <span className="grid gap-4 justify-items-center">
        <span className="flex size-14 items-center justify-center rounded-full border border-border-soft bg-soft-card text-primary">
          <Plus size={26} />
        </span>
        <span>
          <span className="block text-xl font-semibold text-primary">Crear nueva</span>
          <span className="mt-2 block text-base text-text-secondary">Próximamente</span>
        </span>
      </span>
    </button>
  );
}
