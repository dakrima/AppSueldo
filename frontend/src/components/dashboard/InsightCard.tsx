import { CircleCheck, Lightbulb } from "lucide-react";
import type { Tone } from "@/lib/mock-data";

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
    <aside className={`rounded-2xl border p-6 shadow-[var(--shadow-paper)] ${toneClasses[tone]}`}>
      <div className="flex gap-4">
        <Icon className="mt-1 shrink-0" size={22} />
        <div>
          {title ? <h2 className="text-lg font-semibold">{title}</h2> : null}
          <p className="text-lg leading-8 text-primary">{description}</p>
        </div>
      </div>
    </aside>
  );
}
