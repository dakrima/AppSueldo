export function SessionLoading() {
  return (
    <div className="grid min-h-[50vh] place-items-center px-4">
      <div className="rounded-2xl border border-border-soft bg-soft-card p-6 text-center shadow-[var(--shadow-paper)]">
        <p className="text-lg font-semibold text-primary">Validando sesión</p>
        <p className="mt-2 text-sm text-text-secondary">Preparando tu espacio financiero...</p>
      </div>
    </div>
  );
}
