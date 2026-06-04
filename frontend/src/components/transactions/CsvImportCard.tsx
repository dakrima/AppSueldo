"use client";

import { ChangeEvent, FormEvent, useState } from "react";
import { FileUp } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/ErrorMessage";
import { LoadingState } from "@/components/ui/LoadingState";
import { SuccessState } from "@/components/ui/SuccessState";
import { importTransactionsCsv } from "@/features/transactions/api";
import type { ImportBatch } from "@/types/finance";

export function CsvImportCard() {
  const [file, setFile] = useState<File | null>(null);
  const [batch, setBatch] = useState<ImportBatch | null>(null);
  const [error, setError] = useState("");
  const [isImporting, setIsImporting] = useState(false);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    setFile(event.target.files?.[0] ?? null);
    setBatch(null);
    setError("");
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!file) {
      setError("Selecciona un archivo CSV antes de importar.");
      return;
    }

    setIsImporting(true);
    setError("");
    setBatch(null);
    try {
      setBatch(await importTransactionsCsv(file));
    } catch (error) {
      setError(error instanceof Error ? error.message : "No pudimos importar el CSV.");
    } finally {
      setIsImporting(false);
    }
  }

  return (
    <section className="rounded-2xl border border-border-soft bg-soft-card p-5 shadow-[var(--shadow-paper)] sm:p-7">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 className="flex items-center gap-3 text-xl font-semibold text-primary">
            <FileUp size={22} />
            Importar CSV
          </h2>
          <p className="mt-2 max-w-2xl text-base leading-6 text-text-secondary">
            Sube movimientos manuales con columnas fecha, monto y descripcion. Moneda y tipo son opcionales.
          </p>
        </div>
        <span className="inline-flex w-fit items-center rounded-md bg-amber-bg px-2.5 py-1 text-xs font-bold text-[#7a4b00]">
          MVP manual
        </span>
      </div>

      <form onSubmit={handleSubmit} className="mt-5 grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
        <label className="grid gap-2 text-sm font-medium text-primary">
          Archivo CSV
          <input
            type="file"
            accept=".csv,text/csv"
            onChange={handleFileChange}
            className="h-12 rounded-lg border border-border-soft bg-soft-card px-4 py-2 text-sm text-primary file:mr-4 file:rounded-md file:border-0 file:bg-muted-surface file:px-3 file:py-1.5 file:text-sm file:font-semibold file:text-primary"
          />
          <span className="text-xs leading-5 text-text-secondary">
            Se importará en tu cuenta manual y se omitirán filas repetidas.
          </span>
        </label>
        <Button type="submit" disabled={isImporting}>
          <FileUp size={20} />
          {isImporting ? "Importando..." : "Importar archivo"}
        </Button>
      </form>

      {isImporting ? (
        <div className="mt-5">
          <LoadingState title="Importando movimientos" description="Validando filas y evitando duplicados..." />
        </div>
      ) : null}

      {error ? (
        <div className="mt-5">
          <ErrorMessage title="No pudimos importar el CSV" message={error} />
        </div>
      ) : null}

      {batch ? (
        <div className="mt-5 grid gap-4">
          <SuccessState title="Importación completada" message={`Archivo procesado: ${batch.originalFilename}.`} />
          <div className="grid gap-3 sm:grid-cols-3">
            <ImportCount label="Creadas" value={batch.createdCount} />
            <ImportCount label="Omitidas" value={batch.skippedCount} />
            <ImportCount label="Inválidas" value={batch.invalidCount} />
          </div>
        </div>
      ) : null}
    </section>
  );
}

function ImportCount({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-border-soft bg-warm-canvas/60 p-4">
      <p className="text-xs font-bold uppercase tracking-[0.12em] text-text-muted">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-primary">{value}</p>
    </div>
  );
}
