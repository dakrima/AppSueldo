import { CircleDollarSign } from "lucide-react";

type EmptyStateProps = {
  title: string;
  description: string;
};

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-xl border border-dashed border-border-strong bg-soft-card p-8 text-center">
      <CircleDollarSign className="mx-auto text-text-muted" size={32} />
      <h2 className="mt-4 font-semibold text-primary">{title}</h2>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-text-secondary">{description}</p>
    </div>
  );
}
