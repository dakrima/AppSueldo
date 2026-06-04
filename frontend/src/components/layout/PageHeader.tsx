import { ReactNode } from "react";

type PageHeaderProps = {
  eyebrow?: string;
  title: string;
  description?: string;
  action?: ReactNode;
  variant?: "standard" | "compact";
};

const titleClasses = {
  standard: "text-4xl sm:text-5xl",
  compact: "text-3xl sm:text-4xl",
};

const descriptionClasses = {
  standard: "text-lg leading-7",
  compact: "text-base leading-6",
};

export function PageHeader({ eyebrow, title, description, action, variant = "standard" }: PageHeaderProps) {
  return (
    <header className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
      <div className="grid gap-2">
        {eyebrow ? (
          <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{eyebrow}</p>
        ) : null}
        <h1 className={`${titleClasses[variant]} font-semibold leading-tight tracking-normal text-primary`}>{title}</h1>
        {description ? (
          <p className={`max-w-2xl text-text-secondary ${descriptionClasses[variant]}`}>{description}</p>
        ) : null}
      </div>
      {action ? <div className="shrink-0">{action}</div> : null}
    </header>
  );
}
