import { CheckCircle2 } from "lucide-react";

type SuccessStateProps = {
  title?: string;
  message: string;
};

export function SuccessState({ title = "Listo", message }: SuccessStateProps) {
  return (
    <div className="rounded-lg border border-green-300 bg-mint-bg p-4 text-secondary" role="status">
      <div className="flex gap-2">
        <CheckCircle2 className="mt-0.5 shrink-0" size={20} />
        <div>
          <p className="font-semibold">{title}</p>
          <p className="mt-1 text-sm leading-5">{message}</p>
        </div>
      </div>
    </div>
  );
}
