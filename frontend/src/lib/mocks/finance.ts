import type { BankAccount, BankConnection, Category, MonthlySummary, Transaction } from "@/types/finance";

const manualAccount: BankAccount = {
  id: 1,
  name: "Cuenta manual",
  accountType: "MANUAL",
  currency: "CLP",
  balance: 317700,
};

export const mockBankConnections: BankConnection[] = [
  {
    id: 1,
    provider: "MANUAL",
    institutionName: "Manual",
    status: "ACTIVE",
    accounts: [manualAccount],
  },
];

export const mockCategories: Category[] = [
  {
    id: 1,
    name: "Sueldo",
    type: "INCOME",
    color: "#0f766e",
    icon: "banknote",
  },
  {
    id: 2,
    name: "Vivienda",
    type: "EXPENSE",
    color: "#d97706",
    icon: "home",
  },
  {
    id: 3,
    name: "Alimentación",
    type: "EXPENSE",
    color: "#dc2626",
    icon: "fork-knife",
  },
  {
    id: 4,
    name: "Movilidad",
    type: "EXPENSE",
    color: "#d29437",
    icon: "bus",
  },
  {
    id: 5,
    name: "Ahorro",
    type: "EXPENSE",
    color: "#0ea5e9",
    icon: "piggy-bank",
  },
  {
    id: 6,
    name: "Salidas",
    type: "EXPENSE",
    color: "#be123c",
    icon: "ticket",
  },
  {
    id: 7,
    name: "Suscripciones",
    type: "EXPENSE",
    color: "#7c3aed",
    icon: "clapperboard",
  },
  {
    id: 8,
    name: "Salud",
    type: "EXPENSE",
    color: "#16a34a",
    icon: "heart-pulse",
  },
];

export const mockTransactions: Transaction[] = [
  transaction(1, "Sueldo mensual", 850000, "2026-06-02", "INCOME", 1),
  transaction(2, "Supermercado", 46800, "2026-06-05", "EXPENSE", 3),
  transaction(3, "Metro / transporte", 12000, "2026-06-08", "EXPENSE", 4),
  transaction(4, "Transferencia ahorro", 80000, "2026-06-10", "TRANSFER", 5),
  transaction(5, "Café con amigos", 9500, "2026-06-12", "EXPENSE", 6),
  transaction(6, "Suscripción streaming", 28900, "2026-06-13", "EXPENSE", 7),
  transaction(7, "Entrada cine", 15000, "2026-06-15", "EXPENSE", 6),
];

export const mockMonthlySummary: MonthlySummary = {
  monthlyIncome: 850000,
  monthlyExpenses: 412300,
  estimatedSavings: 120000,
  availableBalance: 317700,
  monthlyBalance: 437700,
  transactionCount: mockTransactions.length,
};

function transaction(
  id: number,
  description: string,
  amount: number,
  transactionDate: string,
  type: Transaction["type"],
  categoryId: number,
): Transaction {
  const category = mockCategories.find((item) => item.id === categoryId) ?? null;

  return {
    id,
    categoryId: category?.id ?? null,
    categoryName: category?.name ?? null,
    category,
    bankAccountId: manualAccount.id,
    bankAccount: manualAccount,
    amount,
    currency: "CLP",
    description,
    transactionDate,
    type,
    source: "MANUAL",
    notes: null,
  };
}
