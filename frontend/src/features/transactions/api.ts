import { apiFetch } from "@/lib/api/client";
import type { Transaction, TransactionSource, TransactionType } from "@/types/finance";

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
