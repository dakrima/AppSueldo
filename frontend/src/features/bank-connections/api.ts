import { apiFetch } from "@/lib/api/client";
import type { BankConnection, CreateFintocLinkIntentResponse } from "@/types/finance";

export function listBankConnections() {
  return apiFetch<BankConnection[]>("/api/bank-connections");
}

export function createFintocLinkIntent() {
  return apiFetch<CreateFintocLinkIntentResponse>("/api/bank-connections/fintoc/link-intents", {
    method: "POST",
  });
}

export function exchangeFintocToken(exchangeToken: string) {
  return apiFetch<BankConnection>("/api/bank-connections/fintoc/exchange", {
    method: "POST",
    body: JSON.stringify({ exchangeToken }),
  });
}
