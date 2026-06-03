import { LucideIcon } from "lucide-react";

type SummaryCardProps = {
  label: string;
  value: string;
  helper: string;
  tone: "emerald" | "amber" | "slate";
  icon: LucideIcon;
};

const toneClasses = {
  emerald: "bg-emerald-50 text-emerald-700",
  amber: "bg-amber-50 text-amber-700",
  slate: "bg-slate-100 text-slate-700",
};

export function SummaryCard({ label, value, helper, tone, icon: Icon }: SummaryCardProps) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm sm:p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-2 text-xl font-semibold tracking-normal text-slate-950 sm:text-2xl">{value}</p>
        </div>
        <span className={`hidden size-10 items-center justify-center rounded-lg sm:flex ${toneClasses[tone]}`}>
          <Icon size={20} />
        </span>
      </div>
      <p className="mt-4 text-sm text-slate-500">{helper}</p>
    </article>
  );
}
