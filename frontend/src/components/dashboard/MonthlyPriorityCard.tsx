import Link from "next/link";
import { AlertTriangle, ArrowUpRight, CheckCircle2, Info } from "lucide-react";
import type { MonthlyPriorityViewModel, Tone } from "@/types/presentation";

type MonthlyPriorityCardProps = {
  priority: MonthlyPriorityViewModel;
};

const panelClasses: Record<Tone, string> = {
  income: "border-green-300 bg-mint-bg text-secondary",
  expense: "border-red-200 bg-soft-coral-bg text-muted-coral",
  transfer: "border-cyan-200 bg-soft-blue-bg text-primary-container",
  amber: "border-yellow-200 bg-amber-bg text-[#7a4b00]",
  neutral: "border-border-soft bg-soft-card text-primary",
};

const actionClasses: Record<Tone, string> = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-primary",
};

const analysisClasses: Record<Tone, string> = {
  income: "border-green-200 bg-white/45 text-secondary",
  expense: "border-red-200 bg-white/45 text-muted-coral",
  transfer: "border-cyan-200 bg-white/45 text-primary-container",
  amber: "border-yellow-200 bg-white/50 text-[#7a4b00]",
  neutral: "border-white/45 bg-white/45 text-primary",
};

function PriorityIcon({ tone }: { tone: Tone }) {
  if (tone === "expense" || tone === "amber") {
    return <AlertTriangle size={20} aria-hidden="true" />;
  }
  if (tone === "income") {
    return <CheckCircle2 size={20} aria-hidden="true" />;
  }
  return <Info size={20} aria-hidden="true" />;
}

export function MonthlyPriorityCard({ priority }: MonthlyPriorityCardProps) {
  return (
    <section className={`rounded-2xl border p-5 shadow-[var(--shadow-paper)] sm:p-6 ${panelClasses[priority.tone]}`}>
      <div className="flex gap-3">
        <span className="mt-0.5 shrink-0">
          <PriorityIcon tone={priority.tone} />
        </span>
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[0.12em]">Qué revisar ahora</p>
          <h2 className="mt-2 text-xl font-semibold text-primary">{priority.title}</h2>
          <p className="mt-2 text-sm leading-6 text-primary">{priority.description}</p>
          <p className="mt-2 text-sm leading-6 text-primary/75">{priority.whyItMatters}</p>
        </div>
      </div>

      <div className={`mt-5 rounded-xl border px-4 py-3 ${analysisClasses[priority.analysisStatus.tone]}`}>
        <p className="text-xs font-bold uppercase tracking-[0.12em]">{priority.analysisStatus.label}</p>
        <p className="mt-1 text-sm leading-6 text-primary/75">{priority.analysisStatus.description}</p>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        {priority.actions.map((action) => {
          const Icon = action.icon;

          return (
            <Link
              key={action.id}
              href={action.href ?? "/transactions"}
              className="flex min-w-0 gap-3 rounded-xl border border-white/45 bg-white/45 p-4 transition hover:bg-white/70"
            >
              <span className={`grid size-9 shrink-0 place-items-center rounded-lg ${actionClasses[action.tone]}`}>
                <Icon size={17} aria-hidden="true" />
              </span>
              <span className="min-w-0">
                <span className="block text-sm font-semibold text-primary">{action.ctaLabel ?? action.title}</span>
                <span className="mt-1 block text-xs leading-5 text-primary/70">{action.description}</span>
                <span className="mt-2 inline-flex items-center gap-1 text-xs font-bold text-primary">
                  Abrir
                  <ArrowUpRight size={13} aria-hidden="true" />
                </span>
              </span>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
