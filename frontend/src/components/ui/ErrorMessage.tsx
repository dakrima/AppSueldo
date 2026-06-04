import { AlertCircle } from "lucide-react";

type ErrorMessageProps = {
  id?: string;
  title?: string;
  message: string;
};

export function ErrorMessage({ id, title = "No pudimos completar la acción", message }: ErrorMessageProps) {
  return (
    <div
      id={id}
      className="rounded-lg border border-red-200 bg-soft-coral-bg p-3 text-danger"
      role="alert"
    >
      <div className="flex gap-2">
        <AlertCircle className="mt-0.5 shrink-0" size={18} />
        <div>
          <p className="text-sm font-semibold">{title}</p>
          <p className="mt-1 text-sm leading-5">{message}</p>
        </div>
      </div>
    </div>
  );
}
