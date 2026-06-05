import {
  ArrowRightLeft,
  Banknote,
  Bus,
  Clapperboard,
  Coffee,
  ForkKnife,
  HeartPulse,
  Home,
  PiggyBank,
  ReceiptText,
  ShoppingCart,
  Ticket,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { mockBankConnections, mockCategories, mockMonthlySummary, mockTransactions } from "@/lib/mocks/finance";
import type {
  BalanceOverviewViewModel,
  CategoryBreakdownItem,
  CategoryCardModel,
  InsightViewModel,
  MainBalanceViewModel,
  NewTransactionCategoryOption,
  PeriodSummaryViewModel,
  SummaryCardModel,
  Tone,
  TransactionListItem,
} from "@/types/presentation";
import type { Category, Transaction } from "@/types/finance";

const clpFormatter = new Intl.NumberFormat("es-CL", {
  style: "currency",
  currency: "CLP",
  maximumFractionDigits: 0,
});

const categoryIcons: Record<number, LucideIcon> = {
  1: Banknote,
  2: Home,
  3: ForkKnife,
  4: Bus,
  5: PiggyBank,
  6: Ticket,
  7: Clapperboard,
  8: HeartPulse,
};

const categoryTones: Record<number, Tone> = {
  1: "income",
  2: "expense",
  3: "expense",
  4: "amber",
  5: "transfer",
  6: "expense",
  7: "expense",
  8: "expense",
};

export const transactionListItems: TransactionListItem[] = mockTransactions.map(toTransactionListItem);

export const recentTransactionListItems = transactionListItems.slice(0, 5);

const transferTotal = mockTransactions
  .filter((transaction) => transaction.type === "TRANSFER")
  .reduce((total, transaction) => total + transaction.amount, 0);

const accountItems = mockBankConnections.flatMap((connection) =>
  connection.accounts.map((account) => ({
    id: account.id,
    name: account.name,
    kind: accountKindLabel(account.accountType),
    balance: account.balance === null ? "Saldo no disponible" : formatClp(account.balance),
    currency: account.currency,
  })),
);

const consolidatedBalance = mockBankConnections
  .flatMap((connection) => connection.accounts)
  .reduce((total, account) => total + (account.balance ?? 0), 0);

export const balanceOverview: BalanceOverviewViewModel = {
  label: "Saldo disponible hoy",
  total: formatClp(consolidatedBalance),
  helper: "Actualizado con movimientos registrados hasta hoy.",
  updatedCopy: "Composición por cuentas registradas.",
  transferNote: transferTotal > 0 ? `Transferencias registradas aparte: ${formatClp(transferTotal)}.` : undefined,
  accounts: accountItems,
};

export const categoryCardItems: CategoryCardModel[] = [
  categoryCard(1, "+$850.000", 100),
  categoryCard(2, "-$250.000", 62),
  categoryCard(3, "-$180.500", 46),
  categoryCard(4, "-$45.000", 22),
  categoryCard(6, "-$35.000", 14),
  categoryCard(7, "-$28.900", 9),
  categoryCard(8, "-$15.000", 5),
];

export const mainBalance: MainBalanceViewModel = {
  month: "Junio 2026",
  label: "Saldo disponible estimado",
  value: formatClp(mockMonthlySummary.availableBalance),
  helper: "Considera ingresos y gastos reales registrados. Las transferencias se muestran aparte para no inflar el gasto.",
  supportingFacts: [
    { label: "Base del saldo", value: "Ingresos menos gastos reales" },
    { label: "Transferencias", value: "Separadas del gasto" },
  ],
};

export const dashboardSummaryCards: SummaryCardModel[] = [
  {
    label: "Ingresos del mes",
    value: `+${formatClp(mockMonthlySummary.monthlyIncome)}`,
    helper: "Sueldo y entradas registradas",
    tone: "income",
    icon: Banknote,
  },
  {
    label: "Egresos del mes",
    value: `-${formatClp(mockMonthlySummary.monthlyExpenses)}`,
    helper: "Salida real, sin transferencias",
    tone: "expense",
    icon: ReceiptText,
  },
  {
    label: "Disponible para ahorrar",
    value: formatClp(mockMonthlySummary.estimatedSavings),
    helper: "Estimación del periodo",
    tone: "neutral",
    icon: PiggyBank,
  },
];

export const categoryBreakdown: CategoryBreakdownItem[] = [
  { name: "Vivienda", amount: "$250.000", percent: 100, tone: "neutral" },
  { name: "Alimentación", amount: "$180.500", percent: 72, tone: "expense" },
  { name: "Movilidad", amount: "$45.000", percent: 18, tone: "amber" },
];

export const insights: InsightViewModel[] = [
  {
    title: "Vas bien",
    description: "Tus gastos fijos principales ya están cubiertos.",
    tone: "income",
  },
  {
    title: "Ojo con alimentación",
    description: "Gastaste un 12% más en comida que el mes pasado, pero sigues dentro del margen.",
    tone: "amber",
  },
];

export const periodSummary: PeriodSummaryViewModel = {
  income: "+$850.000",
  expenses: "-$58.800",
  note: "Transferencias registradas aparte: $80.000.",
};

export const newTransactionCategoryOptions: NewTransactionCategoryOption[] = [
  { name: "Alimentación", icon: ForkKnife },
  { name: "Vivienda", icon: Home },
  { name: "Movilidad", icon: Bus },
  { name: "Ocio", icon: Ticket },
  { name: "Ahorro", icon: PiggyBank },
];

function toTransactionListItem(transaction: Transaction): TransactionListItem {
  const categoryId = transaction.category?.id;
  const tone = categoryId ? categoryTones[categoryId] : "neutral";
  const Icon = transactionIcon(transaction, categoryId);

  return {
    id: transaction.id,
    description: transaction.description,
    category: transaction.categoryName ?? "Sin categoría",
    amount: signedAmount(transaction),
    date: formatShortDate(transaction.transactionDate),
    typeLabel: transactionTypeLabel(transaction),
    type: transaction.type,
    source: transaction.source,
    currency: transaction.currency,
    bankAccountName: transaction.bankAccount?.name ?? "Cuenta manual",
    icon: Icon,
    tone,
  };
}

function categoryCard(categoryId: number, amount: string, percent: number): CategoryCardModel {
  const category = requiredCategory(categoryId);

  return {
    id: category.id,
    name: category.name,
    type: category.type,
    amount,
    percent,
    icon: categoryIcons[category.id],
    tone: categoryTones[category.id],
  };
}

function requiredCategory(categoryId: number): Category {
  const category = mockCategories.find((item) => item.id === categoryId);
  if (!category) {
    throw new Error(`Missing mock category ${categoryId}.`);
  }
  return category;
}

function signedAmount(transaction: Transaction) {
  if (transaction.type === "INCOME") {
    return `+${formatClp(transaction.amount)}`;
  }
  if (transaction.type === "TRANSFER") {
    return formatClp(transaction.amount);
  }
  return `-${formatClp(transaction.amount)}`;
}

function transactionTypeLabel(transaction: Transaction) {
  if (transaction.type === "INCOME") {
    return "Ingreso";
  }
  if (transaction.type === "TRANSFER") {
    return "Transferencia";
  }
  return "Gasto";
}

function formatClp(value: number) {
  return clpFormatter.format(value);
}

function accountKindLabel(accountType: string | null) {
  if (!accountType || accountType === "MANUAL") {
    return "Cuenta manual";
  }
  return accountType;
}

function formatShortDate(value: string) {
  const [, month, day] = value.split("-").map(Number);
  const months = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic"];
  return `${day.toString().padStart(2, "0")} ${months[month - 1]}`;
}

function transactionIcon(transaction: Transaction, categoryId?: number) {
  const description = transaction.description.toLowerCase();
  if (description.includes("supermercado")) {
    return ShoppingCart;
  }
  if (description.includes("café")) {
    return Coffee;
  }
  if (transaction.type === "TRANSFER") {
    return ArrowRightLeft;
  }
  if (categoryId) {
    return categoryIcons[categoryId];
  }
  if (transaction.type === "INCOME") {
    return Banknote;
  }
  return ReceiptText;
}
