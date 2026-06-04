import {
  newTransactionCategoryOptions,
  periodSummary,
  recentTransactionListItems,
  transactionListItems,
} from "@/lib/mocks/presentation";
import { mockTransactions } from "@/lib/mocks/finance";

export function getTransactionsData() {
  return transactionListItems;
}

export function getRecentTransactionsData() {
  return recentTransactionListItems;
}

export function getPeriodSummaryData() {
  return periodSummary;
}

export function getNewTransactionCategoryOptions() {
  return newTransactionCategoryOptions;
}

export function getTransactionDomainMocks() {
  return mockTransactions;
}
