import { InputHTMLAttributes } from "react";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
};

export function Input({ label, className, ...props }: InputProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-700">
      {label}
      <input
        className={[
          "h-12 rounded-lg border border-border-soft bg-soft-card px-4 text-sm text-primary outline-none transition placeholder:text-text-muted focus:border-border-strong focus:ring-2 focus:ring-mint-bg disabled:bg-muted-surface disabled:text-text-muted",
          className,
        ]
          .filter(Boolean)
          .join(" ")}
        {...props}
      />
    </label>
  );
}
