"use client";

import { InputHTMLAttributes, useId } from "react";
import { ErrorMessage } from "@/components/ui/ErrorMessage";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  helperText?: string;
  error?: string;
};

export function Input({ label, helperText, error, className, id, ...props }: InputProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const helperId = helperText ? `${inputId}-helper` : undefined;
  const errorId = error ? `${inputId}-error` : undefined;
  const describedBy = [helperId, errorId].filter(Boolean).join(" ") || undefined;

  return (
    <div className="grid gap-2">
      {label ? (
        <label htmlFor={inputId} className="text-sm font-medium text-primary">
          {label}
        </label>
      ) : null}
      <input
        id={inputId}
        aria-describedby={describedBy}
        aria-invalid={error ? true : undefined}
        className={[
          "h-12 rounded-lg border bg-soft-card px-4 text-sm text-primary outline-none transition placeholder:text-text-muted focus:border-border-strong focus:ring-2 focus:ring-mint-bg disabled:bg-muted-surface disabled:text-text-muted",
          error ? "border-red-200 focus:ring-soft-coral-bg" : "border-border-soft",
          className,
        ]
          .filter(Boolean)
          .join(" ")}
        {...props}
      />
      {helperText ? (
        <p id={helperId} className="text-xs leading-5 text-text-secondary">
          {helperText}
        </p>
      ) : null}
      {error ? <ErrorMessage id={errorId} title="Revisa este campo" message={error} /> : null}
    </div>
  );
}
