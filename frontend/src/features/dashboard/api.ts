import { apiFetch } from "@/lib/api/client";
import type { MonthlySummary } from "@/types/finance";

export function getMonthlySummary() {
  return apiFetch<MonthlySummary>("/api/dashboard/monthly-summary");
}
