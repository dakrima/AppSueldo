"use client";

import Link from "next/link";
import { CalendarDays, CheckCircle2, X } from "lucide-react";
import { useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { getNewTransactionCategoryOptions } from "@/features/transactions/data";

const transactionTypes = ["Gasto", "Ingreso", "Transferencia"];

export default function NewTransactionPage() {
  const [type, setType] = useState("Gasto");
  const [category, setCategory] = useState("Movilidad");
  const [saved, setSaved] = useState(false);
  const newTransactionCategories = getNewTransactionCategoryOptions();

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaved(true);
  }

  return (
    <AppShell title="Nuevo movimiento" hideHeader>
      <div className="mx-auto grid min-h-[calc(100vh-8rem)] w-full max-w-2xl place-items-center">
        <form
          onSubmit={handleSubmit}
          className="rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[0_24px_60px_rgb(0_38_39_/_0.10)] sm:p-8"
        >
          <div className="flex items-start justify-between gap-4">
            <div>
              <h2 className="text-3xl font-semibold">Nuevo movimiento</h2>
              <p className="mt-2 text-lg text-text-secondary">Registra tus ingresos o gastos del mes.</p>
            </div>
            <Button asChild variant="ghost" size="sm" aria-label="Cerrar">
              <Link href="/transactions">
                <X size={22} />
              </Link>
            </Button>
          </div>

          <div className="mt-8 grid grid-cols-3 rounded-lg bg-muted-surface p-1">
            {transactionTypes.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => setType(item)}
                className={`h-12 rounded-md text-sm font-semibold transition sm:text-base ${
                  type === item ? "bg-soft-card text-primary shadow-[var(--shadow-paper)]" : "text-text-secondary"
                }`}
              >
                {item}
              </button>
            ))}
          </div>

          <div className="mt-10 text-center">
            <label className="text-xs font-bold uppercase tracking-[0.12em] text-text-secondary">Monto</label>
            <input
              inputMode="numeric"
              placeholder="$0"
              className="mt-2 w-full bg-transparent text-center text-6xl font-semibold tracking-normal text-primary outline-none placeholder:text-text-muted/60"
            />
          </div>

          <div className="mt-8 grid gap-5">
            <Input label="Descripción" placeholder="Ej: Supermercado, Sueldo..." />

            <div>
              <p className="text-sm font-semibold text-primary">Categoría</p>
              <div className="mt-3 flex flex-wrap gap-3">
                {newTransactionCategories.map((item) => {
                  const Icon = item.icon;
                  const active = category === item.name;
                  return (
                    <button
                      key={item.name}
                      type="button"
                      onClick={() => setCategory(item.name)}
                      className={`inline-flex h-11 items-center gap-2 rounded-full border px-4 text-sm font-semibold transition ${
                        active
                          ? "border-secondary bg-mint-bg text-secondary"
                          : "border-border-soft bg-soft-card text-text-secondary hover:border-border-strong"
                      }`}
                    >
                      <Icon size={18} />
                      {item.name}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <label className="grid gap-2 text-sm font-semibold text-primary">
                Fecha
                <span className="flex h-12 items-center justify-between rounded-lg border border-border-soft bg-soft-card px-4 text-base font-normal">
                  10/06/2026
                  <CalendarDays size={20} />
                </span>
              </label>
              <Input label="Nota (opcional)" placeholder="Detalles extra..." />
            </div>
          </div>

          {saved ? (
            <div className="mt-7 rounded-lg border border-green-300 bg-mint-bg p-4 text-secondary">
              <p className="flex items-center gap-2 font-semibold">
                <CheckCircle2 size={20} />
                Movimiento listo para conectar con POST /api/transactions.
              </p>
            </div>
          ) : null}

          <div className="mt-8 flex flex-col gap-3 border-t border-border-soft pt-6 sm:flex-row sm:justify-end">
            <Button asChild variant="secondary">
              <Link href="/transactions">Cancelar</Link>
            </Button>
            <Button type="submit">Guardar movimiento</Button>
          </div>
        </form>
      </div>
    </AppShell>
  );
}
