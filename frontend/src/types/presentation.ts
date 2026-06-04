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

export type MainBalanceViewModel = {
  month: string;
  label: string;
  value: string;
  helper: string;
};

export type CategoryBreakdownItem = {
  name: string;
  amount: string;
  percent: number;
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
