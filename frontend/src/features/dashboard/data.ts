import {
  categoryBreakdown,
  dashboardSummaryCards,
  insights,
  mainBalance,
} from "@/lib/mocks/presentation";
import { mockMonthlySummary } from "@/lib/mocks/finance";

export function getDashboardSummaryCards() {
  return dashboardSummaryCards;
}

export function getDashboardInsights() {
  return insights;
}

export function getMainBalanceData() {
  return mainBalance;
}

export function getCategoryBreakdownData() {
  return categoryBreakdown;
}

export function getMonthlySummaryMock() {
  return mockMonthlySummary;
}
