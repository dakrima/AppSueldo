import { CircleCheck, Lightbulb } from "lucide-react";
import type { Tone } from "@/types/presentation";

type InsightCardProps = {
  title?: string;
  description: string;
  tone?: Tone;
};

const toneClasses = {
  income: "border-green-300 bg-mint-bg text-secondary",
  expense: "border-red-200 bg-soft-coral-bg text-muted-coral",
  transfer: "border-cyan-200 bg-soft-blue-bg text-primary-container",
  amber: "border-yellow-200 bg-amber-bg text-[#7a4b00]",
  neutral: "border-border-soft bg-soft-card text-primary",
};

export function InsightCard({ title, description, tone = "amber" }: InsightCardProps) {
  const Icon = tone === "income" ? CircleCheck : Lightbulb;

  return (
    <aside className={`rounded-2xl border p-5 shadow-[var(--shadow-paper)] sm:p-6 ${toneClasses[tone]}`}>
      <div className="flex gap-3">
        <Icon className="mt-1 shrink-0" size={20} aria-hidden="true" />
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em]">Lectura del mes</p>
          {title ? <h2 className="mt-2 text-lg font-semibold">{title}</h2> : null}
          <p className="mt-1 text-sm leading-6 text-primary">{description}</p>
        </div>
      </div>
    </aside>
  );
}
