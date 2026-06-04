import { LoaderCircle } from "lucide-react";

type LoadingStateProps = {
  title: string;
  description?: string;
};

export function LoadingState({ title, description }: LoadingStateProps) {
  return (
    <div className="rounded-2xl border border-border-soft bg-soft-card p-6 text-center shadow-[var(--shadow-paper)]">
      <LoaderCircle className="mx-auto animate-spin text-text-muted" size={28} aria-hidden="true" />
      <p className="mt-4 text-lg font-semibold text-primary">{title}</p>
      {description ? <p className="mt-2 text-sm text-text-secondary">{description}</p> : null}
    </div>
  );
}
