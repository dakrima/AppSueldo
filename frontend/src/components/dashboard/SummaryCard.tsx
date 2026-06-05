import type { LucideIcon } from "lucide-react";
import type { Tone } from "@/types/presentation";

type SummaryCardProps = {
  label: string;
  value: string;
  helper?: string;
  tone: Tone;
  icon: LucideIcon;
};

const toneClasses = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-text-secondary",
};

const valueClasses = {
  income: "text-secondary",
  expense: "text-muted-coral",
  transfer: "text-primary-container",
  amber: "text-text-secondary",
  neutral: "text-primary",
};

export function SummaryCard({ label, value, helper, tone, icon: Icon }: SummaryCardProps) {
  return (
    <article className="min-w-0 rounded-xl border border-border-soft bg-soft-card p-4 shadow-[var(--shadow-paper)] sm:p-5">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{label}</p>
          <p className={`mt-3 text-2xl font-semibold tracking-normal ${valueClasses[tone]}`}>{value}</p>
        </div>
        <span className={`flex size-9 shrink-0 items-center justify-center rounded-lg ${toneClasses[tone]}`}>
          <Icon size={17} />
        </span>
      </div>
      {helper ? <p className="mt-2 text-sm leading-5 text-text-secondary">{helper}</p> : null}
    </article>
  );
}
