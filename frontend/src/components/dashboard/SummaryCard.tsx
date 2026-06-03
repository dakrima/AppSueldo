import type { LucideIcon } from "lucide-react";
import type { Tone } from "@/lib/mock-data";

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
  transfer: "text-primary",
  amber: "text-text-secondary",
  neutral: "text-primary",
};

export function SummaryCard({ label, value, helper, tone, icon: Icon }: SummaryCardProps) {
  return (
    <article className="rounded-xl border border-border-soft bg-soft-card p-4 shadow-[var(--shadow-paper)] sm:p-5">
      <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.12em] text-text-secondary">
        <span className={`flex size-8 items-center justify-center rounded-lg ${toneClasses[tone]}`}>
          <Icon size={17} />
        </span>
        {label}
      </div>
      <p className={`mt-5 text-2xl font-semibold tracking-normal ${valueClasses[tone]}`}>{value}</p>
      {helper ? <p className="mt-2 text-sm leading-5 text-text-secondary">{helper}</p> : null}
    </article>
  );
}
