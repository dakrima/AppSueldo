import { LoadingState } from "@/components/ui/LoadingState";

export function SessionLoading() {
  return (
    <div className="grid min-h-[50vh] place-items-center px-4">
      <LoadingState title="Validando sesión" description="Preparando tu espacio financiero..." />
    </div>
  );
}
