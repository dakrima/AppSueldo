import { apiFetch } from "@/lib/api/client";
import type { ImportBatch, Transaction, TransactionSource, TransactionType } from "@/types/finance";

export type CreateTransactionRequest = {
  bankAccountId?: number | null;
  categoryId?: number | null;
  amount: number;
  currency?: string | null;
  description: string;
  transactionDate: string;
  type: TransactionType;
  source?: TransactionSource | null;
  notes?: string | null;
};

export type AssignTransactionCategoryRequest = {
  categoryId: number | null;
};

export function listTransactions() {
  return apiFetch<Transaction[]>("/api/transactions");
}

export function createTransaction(request: CreateTransactionRequest) {
  return apiFetch<Transaction>("/api/transactions", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function assignTransactionCategory(transactionId: number, request: AssignTransactionCategoryRequest) {
  return apiFetch<Transaction>(`/api/transactions/${transactionId}/category`, {
    method: "PATCH",
    body: JSON.stringify(request),
  });
}

export function importTransactionsCsv(file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return apiFetch<ImportBatch>("/api/transactions/imports", {
    method: "POST",
    body: formData,
  });
}

export function getImportBatch(batchId: number) {
  return apiFetch<ImportBatch>(`/api/transactions/imports/${batchId}`);
}
