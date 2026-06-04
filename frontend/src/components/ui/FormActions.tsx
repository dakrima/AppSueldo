import { ReactNode } from "react";

type FormActionsProps = {
  primary: ReactNode;
  secondary?: ReactNode;
};

export function FormActions({ primary, secondary }: FormActionsProps) {
  return (
    <div className="flex flex-col gap-3 border-t border-border-soft pt-6 sm:flex-row sm:justify-end">
      {secondary}
      {primary}
    </div>
  );
}
