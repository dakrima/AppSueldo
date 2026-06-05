import type { LucideIcon } from "lucide-react";
import type { CategoryType, TransactionSource, TransactionType } from "@/types/finance";

export type Tone = "income" | "expense" | "transfer" | "amber" | "neutral";

export type TransactionListItem = {
  id: number;
  description: string;
  category: string;
  amount: string;
  date: string;
  typeLabel: string;
  type: TransactionType;
  source: TransactionSource;
  currency: string;
  bankAccountName: string;
  icon: LucideIcon;
  tone: Tone;
};

export type CategoryCardModel = {
  id: number;
  name: string;
  type: CategoryType;
  amount: string;
  percent: number;
  icon: LucideIcon;
  tone: Tone;
};

export type SummaryCardModel = {
  label: string;
  value: string;
  helper?: string;
  tone: Tone;
  icon: LucideIcon;
};

export type BalanceAccountViewModel = {
  id: number;
  name: string;
  kind: string;
  institutionName?: string | null;
  balance: string;
  currency: string;
};

export type BalanceOverviewViewModel = {
  label: string;
  total: string;
  helper: string;
  updatedCopy: string;
  transferNote?: string;
  accounts: BalanceAccountViewModel[];
};

export type MainBalanceViewModel = {
  month: string;
  label: string;
  value: string;
  helper: string;
  supportingFacts: Array<{
    label: string;
    value: string;
  }>;
};

export type CategoryBreakdownItem = {
  name: string;
  amount: string;
  percent: number;
  tone: Tone;
};

export type CategoryDistributionItem = {
  id: string;
  name: string;
  type: TransactionType;
  typeLabel: string;
  amount: string;
  rawAmount: number;
  movementCount: number;
  percent: number;
  percentLabel: string;
  lastMovement: string;
  tone: Tone;
};

export type InsightViewModel = {
  title: string;
  description: string;
  tone: Tone;
};

export type PeriodSummaryViewModel = {
  income: string;
  expenses: string;
  note: string;
};

export type NewTransactionCategoryOption = {
  name: string;
  icon: LucideIcon;
};
