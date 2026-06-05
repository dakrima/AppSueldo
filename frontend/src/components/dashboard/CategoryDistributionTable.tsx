import { EmptyState } from "@/components/ui/EmptyState";
import type { CategoryDistributionItem, Tone } from "@/types/presentation";

type CategoryDistributionTableProps = {
  items: CategoryDistributionItem[];
};

const badgeClasses: Record<Tone, string> = {
  income: "bg-mint-bg text-secondary",
  expense: "bg-soft-coral-bg text-muted-coral",
  transfer: "bg-soft-blue-bg text-primary-container",
  amber: "bg-amber-bg text-[#7a4b00]",
  neutral: "bg-muted-surface text-text-secondary",
};

const barClasses: Record<Tone, string> = {
  income: "bg-secondary",
  expense: "bg-muted-coral",
  transfer: "bg-primary-container",
  amber: "bg-[#d29437]",
  neutral: "bg-primary",
};

const amountClasses: Record<Tone, string> = {
  income: "text-secondary",
  expense: "text-muted-coral",
  transfer: "text-primary-container",
  amber: "text-[#7a4b00]",
  neutral: "text-primary",
};

export function CategoryDistributionTable({ items }: CategoryDistributionTableProps) {
  return (
    <section className="min-w-0 rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7">
      <div>
        <h2 className="text-xl font-semibold text-primary">Distribución por categoría</h2>
        <p className="mt-2 text-sm leading-6 text-text-secondary sm:text-base">
          Así se agrupan tus ingresos y gastos registrados este mes.
        </p>
      </div>

      {items.length > 0 ? (
        <>
          <div className="mt-6 hidden overflow-hidden rounded-xl border border-border-soft lg:block">
            <table className="w-full table-fixed text-left">
              <thead className="bg-muted-surface/70 text-xs font-bold uppercase tracking-[0.12em] text-text-muted">
                <tr>
                  <th scope="col" className="w-[28%] px-4 py-3">
                    Categoría
                  </th>
                  <th scope="col" className="w-[16%] px-4 py-3">
                    Tipo
                  </th>
                  <th scope="col" className="w-[14%] px-4 py-3 text-right">
                    Movimientos
                  </th>
                  <th scope="col" className="w-[16%] px-4 py-3 text-right">
                    Monto
                  </th>
                  <th scope="col" className="w-[16%] px-4 py-3">
                    % del total
                  </th>
                  <th scope="col" className="w-[10%] px-4 py-3 text-right">
                    Último
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-soft">
                {items.map((item) => (
                  <tr key={item.id} className="align-middle">
                    <td className="px-4 py-4">
                      <p className="truncate font-semibold text-primary">{item.name}</p>
                    </td>
                    <td className="px-4 py-4">
                      <span className={`inline-flex rounded-md px-2.5 py-1 text-xs font-bold ${badgeClasses[item.tone]}`}>
                        {item.typeLabel}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-right text-sm font-medium text-text-secondary">
                      {item.movementCount}
                    </td>
                    <td className={`px-4 py-4 text-right text-sm font-semibold ${amountClasses[item.tone]}`}>
                      {item.amount}
                    </td>
                    <td className="px-4 py-4">
                      <div className="flex items-center gap-3">
                        <div
                          className="h-2 min-w-0 flex-1 rounded-full bg-muted-surface"
                          aria-label={`${item.percentLabel} del total de ${item.typeLabel.toLowerCase()}`}
                        >
                          <div
                            className={`h-2 rounded-full ${barClasses[item.tone]}`}
                            style={{ width: `${item.percent}%` }}
                          />
                        </div>
                        <span className="w-10 text-right text-xs font-semibold text-text-muted">{item.percentLabel}</span>
                      </div>
                    </td>
                    <td className="px-4 py-4 text-right text-sm text-text-secondary">{item.lastMovement}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-5 grid gap-3 lg:hidden">
            {items.map((item) => (
              <article key={item.id} className="rounded-xl border border-border-soft bg-warm-canvas/50 p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0">
                    <p className="truncate font-semibold text-primary">{item.name}</p>
                    <span className={`mt-2 inline-flex rounded-md px-2.5 py-1 text-xs font-bold ${badgeClasses[item.tone]}`}>
                      {item.typeLabel}
                    </span>
                  </div>
                  <p className={`shrink-0 text-right text-lg font-semibold ${amountClasses[item.tone]}`}>{item.amount}</p>
                </div>
                <div className="mt-4 h-2 rounded-full bg-muted-surface" aria-hidden="true">
                  <div className={`h-2 rounded-full ${barClasses[item.tone]}`} style={{ width: `${item.percent}%` }} />
                </div>
                <div className="mt-3 flex flex-wrap items-center justify-between gap-3 text-xs font-semibold text-text-muted">
                  <span>{item.movementCount} movimientos</span>
                  <span>{item.percentLabel} del total</span>
                  <span>{item.lastMovement}</span>
                </div>
              </article>
            ))}
          </div>
        </>
      ) : (
        <div className="mt-5">
          <EmptyState
            title="Aún no hay movimientos categorizados este mes."
            description="Cuando registres ingresos o gastos, verás cómo se agrupan por categoría."
          />
        </div>
      )}
    </section>
  );
}
