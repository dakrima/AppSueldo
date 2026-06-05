import { Banknote, PiggyBank, ReceiptText } from "lucide-react";
import type { BankConnection, Category, MonthlySummary, Transaction, TransactionType } from "@/types/finance";
import type {
  BalanceOverviewViewModel,
  CategoryBreakdownItem,
  CategoryDistributionItem,
  InsightViewModel,
  SummaryCardModel,
  Tone,
} from "@/types/presentation";

const currencyFormatterCache = new Map<string, Intl.NumberFormat>();
const shortDateFormatter = new Intl.DateTimeFormat("es-CL", {
  day: "2-digit",
  month: "short",
});

export function formatCurrencyCLP(value: number, currency = "CLP") {
  const safeCurrency = currency || "CLP";
  const formatter = getCurrencyFormatter(safeCurrency);
  return formatter.format(value);
}

export function isCurrentMonth(transactionDate: string, referenceDate = new Date()) {
  const date = parseTransactionDate(transactionDate);
  if (!date) {
    return false;
  }

  return date.getFullYear() === referenceDate.getFullYear() && date.getMonth() === referenceDate.getMonth();
}

export function calculateAccountTotal(
  bankConnections: BankConnection[],
  monthlySummary: MonthlySummary,
  transactions: Transaction[] = [],
): BalanceOverviewViewModel {
  const accounts = bankConnections.flatMap((connection) =>
    connection.accounts.map((account) => ({
      id: account.id,
      name: account.name,
      kind: accountKindLabel(account.accountType),
      institutionName: connection.institutionName,
      balance:
        typeof account.balance === "number" && Number.isFinite(account.balance)
          ? formatCurrencyCLP(account.balance, account.currency)
          : "Saldo no disponible",
      currency: account.currency,
      rawBalance: account.balance,
    })),
  );
  const numericBalances = accounts
    .map((account) => account.rawBalance)
    .filter((balance): balance is number => typeof balance === "number" && Number.isFinite(balance));
  const fallbackBalance = numberOrNull(monthlySummary.availableBalance);
  const total = numericBalances.length > 0 ? numericBalances.reduce((sum, balance) => sum + balance, 0) : fallbackBalance ?? 0;
  const transferTotal = transactions
    .filter((transaction) => transaction.type === "TRANSFER" && isCurrentMonth(transaction.transactionDate))
    .reduce((sum, transaction) => sum + transaction.amount, 0);

  const accountItems = accounts.map((account) => ({
    id: account.id,
    name: account.name,
    kind: account.kind,
    institutionName: account.institutionName,
    balance: account.balance,
    currency: account.currency,
  }));

  return {
    label: "Saldo disponible hoy",
    total: formatCurrencyCLP(total),
    helper: "Actualizado con movimientos registrados hasta hoy.",
    updatedCopy:
      numericBalances.length > 0
        ? "Composición por cuentas registradas."
        : "Sin saldo de cuenta disponible; usamos el resumen mensual.",
    transferNote:
      transferTotal > 0
        ? `Transferencias registradas aparte: ${formatCurrencyCLP(transferTotal)}. No se consideran gasto.`
        : undefined,
    accounts: accountItems,
  };
}

export function buildSummaryCards(monthlySummary: MonthlySummary): SummaryCardModel[] {
  const monthlyIncome = numberOrNull(monthlySummary.monthlyIncome) ?? 0;
  const monthlyExpenses = numberOrNull(monthlySummary.monthlyExpenses) ?? 0;
  const estimatedSavings = numberOrNull(monthlySummary.estimatedSavings) ?? monthlyIncome - monthlyExpenses;

  return [
    {
      label: "Ingresos del mes",
      value: `+${formatCurrencyCLP(monthlyIncome)}`,
      helper: "Entradas registradas este mes",
      tone: "income",
      icon: Banknote,
    },
    {
      label: "Egresos del mes",
      value: `-${formatCurrencyCLP(monthlyExpenses)}`,
      helper: "Salida real, sin transferencias",
      tone: "expense",
      icon: ReceiptText,
    },
    {
      label: "Disponible para ahorrar",
      value: formatSignedCurrency(estimatedSavings),
      helper: "Estimación según ingresos y egresos",
      tone: "neutral",
      icon: PiggyBank,
    },
  ];
}

export function calculateCategoryDistribution(
  transactions: Transaction[],
  categories: Category[] = [],
  referenceDate = new Date(),
): CategoryDistributionItem[] {
  const categoryNamesById = new Map(categories.map((category) => [category.id, category.name]));
  const totalsByType: Record<TransactionType, number> = {
    INCOME: 0,
    EXPENSE: 0,
    TRANSFER: 0,
  };
  const groups = new Map<
    string,
    {
      id: string;
      name: string;
      type: TransactionType;
      total: number;
      movementCount: number;
      latestDate: string | null;
    }
  >();

  transactions
    .filter((transaction) => isCurrentMonth(transaction.transactionDate, referenceDate))
    .forEach((transaction) => {
      const categoryId = transaction.category?.id ?? transaction.categoryId;
      const hasCategoryId = categoryId !== null && categoryId !== undefined;
      const categoryName =
        transaction.category?.name ??
        transaction.categoryName ??
        (hasCategoryId ? categoryNamesById.get(categoryId) : undefined) ??
        "Sin categoría";
      const key = hasCategoryId ? `${transaction.type}:category:${categoryId}` : `${transaction.type}:name:${categoryName}`;
      const amount = numberOrNull(transaction.amount) ?? 0;
      const current = groups.get(key) ?? {
        id: key,
        name: categoryName,
        type: transaction.type,
        total: 0,
        movementCount: 0,
        latestDate: null,
      };

      current.total += amount;
      current.movementCount += 1;
      current.latestDate = latestTransactionDate(current.latestDate, transaction.transactionDate);
      totalsByType[transaction.type] += amount;
      groups.set(key, current);
    });

  return Array.from(groups.values())
    .map((group) => {
      const totalForType = totalsByType[group.type];
      const percent = totalForType > 0 ? Math.round((group.total / totalForType) * 100) : 0;

      return {
        id: group.id,
        name: group.name,
        type: group.type,
        typeLabel: getUserFriendlyTransactionType(group.type),
        amount: signedAmount(group.total, group.type),
        rawAmount: group.total,
        movementCount: group.movementCount,
        percent,
        percentLabel: `${percent}%`,
        lastMovement: group.latestDate ? formatShortDate(group.latestDate) : "Sin fecha",
        tone: toneForType(group.type),
      };
    })
    .sort((first, second) => {
      const typeDelta = typeOrder(first.type) - typeOrder(second.type);
      if (typeDelta !== 0) {
        return typeDelta;
      }
      return second.rawAmount - first.rawAmount;
    });
}

export function buildCategoryBreakdownData(
  distribution: CategoryDistributionItem[],
  limit = 5,
): CategoryBreakdownItem[] {
  const expenses = distribution
    .filter((item) => item.type === "EXPENSE")
    .sort((first, second) => second.rawAmount - first.rawAmount)
    .slice(0, limit);
  const maxExpense = expenses[0]?.rawAmount ?? 0;

  return expenses.map((item) => ({
    name: item.name,
    amount: item.amount,
    percent: maxExpense > 0 ? Math.round((item.rawAmount / maxExpense) * 100) : 0,
    tone: item.tone,
  }));
}

export function buildDashboardInsight(
  distribution: CategoryDistributionItem[],
  monthlySummary: MonthlySummary,
): InsightViewModel {
  const topExpense = distribution
    .filter((item) => item.type === "EXPENSE")
    .sort((first, second) => second.rawAmount - first.rawAmount)[0];
  const estimatedSavings = numberOrNull(monthlySummary.estimatedSavings);

  if (topExpense) {
    return {
      title: "Mayor gasto del mes",
      description: `${topExpense.name} concentra ${topExpense.percentLabel} de tus egresos registrados este mes.`,
      tone: topExpense.percent >= 40 ? "amber" : "neutral",
    };
  }

  if (estimatedSavings !== null && estimatedSavings > 0) {
    return {
      title: "Ahorro disponible",
      description: `Tienes ${formatCurrencyCLP(estimatedSavings)} estimados para ahorrar según tu resumen mensual.`,
      tone: "income",
    };
  }

  return {
    title: "Resumen listo para crecer",
    description: "Cuando registres más movimientos, aquí verás una lectura accionable del mes.",
    tone: "neutral",
  };
}

export function getUserFriendlyTransactionType(type: TransactionType) {
  if (type === "INCOME") {
    return "Ingreso";
  }
  if (type === "TRANSFER") {
    return "Transferencia";
  }
  return "Gasto";
}

export function hasFinancialDashboardData(bankConnections: BankConnection[], transactions: Transaction[]) {
  const accountCount = bankConnections.reduce((count, connection) => count + connection.accounts.length, 0);
  return accountCount > 0 || transactions.length > 0;
}

function getCurrencyFormatter(currency: string) {
  const cached = currencyFormatterCache.get(currency);
  if (cached) {
    return cached;
  }

  let formatter: Intl.NumberFormat;
  try {
    formatter = new Intl.NumberFormat("es-CL", {
      style: "currency",
      currency,
      maximumFractionDigits: 0,
    });
  } catch {
    formatter = new Intl.NumberFormat("es-CL", {
      style: "currency",
      currency: "CLP",
      maximumFractionDigits: 0,
    });
  }
  currencyFormatterCache.set(currency, formatter);
  return formatter;
}

function accountKindLabel(accountType: string | null) {
  if (!accountType) {
    return "Cuenta registrada";
  }
  if (accountType === "MANUAL") {
    return "Registro manual";
  }
  return accountType;
}

function signedAmount(amount: number, type: TransactionType) {
  if (type === "INCOME") {
    return `+${formatCurrencyCLP(amount)}`;
  }
  if (type === "EXPENSE") {
    return `-${formatCurrencyCLP(amount)}`;
  }
  return formatCurrencyCLP(amount);
}

function formatSignedCurrency(amount: number) {
  if (amount < 0) {
    return `-${formatCurrencyCLP(Math.abs(amount))}`;
  }
  return formatCurrencyCLP(amount);
}

function toneForType(type: TransactionType): Tone {
  if (type === "INCOME") {
    return "income";
  }
  if (type === "TRANSFER") {
    return "transfer";
  }
  return "expense";
}

function typeOrder(type: TransactionType) {
  if (type === "EXPENSE") {
    return 0;
  }
  if (type === "INCOME") {
    return 1;
  }
  return 2;
}

function numberOrNull(value: unknown) {
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function latestTransactionDate(current: string | null, candidate: string) {
  if (!current) {
    return candidate;
  }
  const currentTime = parseTransactionDate(current)?.getTime() ?? 0;
  const candidateTime = parseTransactionDate(candidate)?.getTime() ?? 0;
  return candidateTime > currentTime ? candidate : current;
}

function formatShortDate(value: string) {
  const date = parseTransactionDate(value);
  return date ? shortDateFormatter.format(date) : "Sin fecha";
}

function parseTransactionDate(value: string) {
  const [datePart] = value.split("T");
  const [year, month, day] = datePart.split("-").map(Number);

  if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
    return new Date(year, month - 1, day);
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}
