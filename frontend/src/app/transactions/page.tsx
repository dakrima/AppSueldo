import { AppShell } from "@/components/layout/AppShell";
import { TransactionList } from "@/components/transactions/TransactionList";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { recentTransactions } from "@/lib/mock-data";

export default function TransactionsPage() {
  return (
    <AppShell title="Movimientos" description="Carga manual inicial para ingresos, gastos y transferencias.">
      <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-3 md:grid-cols-[1fr_180px_160px]">
          <Input placeholder="Buscar por descripcion" />
          <Input placeholder="Categoria" />
          <Button className="justify-center">Agregar movimiento</Button>
        </div>
      </section>
      <TransactionList title="Todos los movimientos" transactions={recentTransactions} />
    </AppShell>
  );
}
