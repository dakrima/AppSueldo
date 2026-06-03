import { ReactNode } from "react";

type BadgeProps = {
  children: ReactNode;
  tone?: "income" | "expense" | "neutral" | "amber";
};

const toneClasses = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-text-secondary",
};

export function Badge({ children, tone = "neutral" }: BadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-md px-2.5 py-1 text-xs font-bold tracking-wide ${toneClasses[tone]}`}>
      {children}
    </span>
  );
}
