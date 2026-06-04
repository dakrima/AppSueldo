import { apiFetch } from "@/lib/api/client";
import type { BankConnection } from "@/types/finance";

export function listBankConnections() {
  return apiFetch<BankConnection[]>("/api/bank-connections");
}
