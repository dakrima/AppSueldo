import {
  ArrowDownCircle,
  ArrowRightLeft,
  ArrowUpCircle,
  Banknote,
  CalendarDays,
  CircleCheck,
  Clock3,
  Landmark,
  ReceiptText,
  Tags,
} from "lucide-react";
import type { BankConnection, Category, MonthlySummary, Transaction, TransactionType } from "@/types/finance";
import type {
  BalanceOverviewViewModel,
  CategoryReadinessViewModel,
  CategoryBreakdownItem,
  CategoryDistributionItem,
  DashboardActionItem,
  FinancialHeroViewModel,
  InsightViewModel,
  MonthlyPriorityViewModel,
  SalaryRhythmViewModel,
  SummaryCardModel,
  Tone,
  TransactionListItem,
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
    label: "Disponible hoy",
    total: formatCurrencyCLP(total),
    rawTotal: total,
    helper: "Esto es lo que tienes disponible hoy en tus cuentas.",
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

export function buildSummaryCards(
  monthlySummary: MonthlySummary,
  transactions: Transaction[] = [],
  bankConnections: BankConnection[] = [],
): SummaryCardModel[] {
  const monthlyIncome = numberOrNull(monthlySummary.monthlyIncome) ?? 0;
  const monthlyExpenses = numberOrNull(monthlySummary.monthlyExpenses) ?? 0;
  const currentMonthTransactions = transactions.filter((transaction) => isCurrentMonth(transaction.transactionDate));
  const reviewCount = countReviewableTransactions(currentMonthTransactions);
  const accountCount = bankConnections.reduce((count, connection) => count + connection.accounts.length, 0);
  const hasConnectionIssue = bankConnections.some((connection) => connection.status === "ERROR");

  return [
    {
      label: "Entró este mes",
      value: `+${formatCurrencyCLP(monthlyIncome)}`,
      helper: monthlyIncome > 0 ? "Ingresos registrados" : "Aún no hay ingresos",
      tone: "income",
      icon: Banknote,
    },
    {
      label: "Gastado este mes",
      value: `-${formatCurrencyCLP(monthlyExpenses)}`,
      helper: monthlyExpenses > 0 ? "Gastos reales, sin transferencias" : "Aún no hay gastos",
      tone: "expense",
      icon: ReceiptText,
    },
    {
      label: "Por revisar",
      value: String(reviewCount),
      helper: reviewCount > 0 ? "Movimientos que necesitan orden" : "Sin pendientes claros",
      context: "Mejora la lectura del mes",
      tone: reviewCount > 0 ? "amber" : "neutral",
      icon: Tags,
    },
    {
      label: "Cuentas conectadas",
      value: String(accountCount),
      helper: hasConnectionIssue
        ? "Hay una cuenta con problema"
        : accountCount > 0
          ? "Incluidas en este resumen"
          : "Sin cuentas conectadas",
      context: hasConnectionIssue ? "Revisar conexión" : "Fuente del resumen financiero",
      tone: hasConnectionIssue ? "amber" : "neutral",
      icon: Landmark,
    },
  ];
}

export function buildFinancialHero(
  balance: BalanceOverviewViewModel,
  monthlySummary: MonthlySummary,
): FinancialHeroViewModel {
  const monthlyIncome = numberOrNull(monthlySummary.monthlyIncome) ?? 0;
  const monthlyExpenses = numberOrNull(monthlySummary.monthlyExpenses) ?? 0;
  const monthlyBalance = numberOrNull(monthlySummary.monthlyBalance) ?? monthlyIncome - monthlyExpenses;
  const availableBalance = numberOrNull(balance.rawTotal) ?? 0;

  if (monthlyBalance > 0) {
    return {
      title: "Estado financiero actual",
      primaryLabel: "Disponible hoy",
      primaryValue: balance.total,
      primaryHelper: "Esto es lo que tienes disponible ahora.",
      monthlyLabel: "Resultado del mes",
      monthlyValue: formatSignedCurrency(monthlyBalance),
      monthlyHelper: "Entró más de lo que salió este mes.",
      headline: "Tienes saldo disponible y tu mes va con flujo positivo.",
      explanation: "Disponible hoy muestra tu plata actual. Resultado del mes muestra si este mes entró más o salió más.",
      transferNote: balance.transferNote,
      tone: "income",
    };
  }

  if (monthlyBalance < 0) {
    return {
      title: "Estado financiero actual",
      primaryLabel: "Disponible hoy",
      primaryValue: balance.total,
      primaryHelper: "Esto es lo que tienes disponible ahora.",
      monthlyLabel: "Resultado del mes",
      monthlyValue: formatSignedCurrency(monthlyBalance),
      monthlyHelper: "Este mes has gastado más de lo que ha ingresado.",
      headline: availableBalance > 0 ? "Tienes saldo disponible, pero tu mes va en negativo." : "Tu saldo y tu flujo mensual necesitan atención.",
      explanation:
        availableBalance > 0
          ? "Tu saldo disponible sigue positivo. El resultado del mes va aparte y muestra que salió más plata de la que entró."
          : "Tu liquidez y el flujo mensual requieren revisión con los movimientos actuales.",
      transferNote: balance.transferNote,
      tone: "expense",
    };
  }

  return {
    title: "Estado financiero actual",
    primaryLabel: "Disponible hoy",
    primaryValue: balance.total,
    primaryHelper: "Esto es lo que tienes disponible ahora.",
    monthlyLabel: "Resultado del mes",
    monthlyValue: formatSignedCurrency(monthlyBalance),
    monthlyHelper: monthlyIncome > 0 || monthlyExpenses > 0 ? "Ingresos y gastos están al mismo nivel." : "Aún faltan movimientos del mes.",
    headline: monthlyIncome > 0 || monthlyExpenses > 0 ? "Tu mes está equilibrado" : "Tu cabina está lista",
    explanation:
      monthlyIncome > 0 || monthlyExpenses > 0
        ? "Disponible hoy muestra tu liquidez actual. Resultado del mes muestra cómo va el flujo de este mes."
        : "Agrega movimientos o conecta tu banco para empezar a ver señales útiles.",
    transferNote: balance.transferNote,
    tone: "neutral",
  };
}

export function buildMonthlyPriority(
  monthlySummary: MonthlySummary,
  transactions: Transaction[],
): MonthlyPriorityViewModel {
  const currentMonthTransactions = transactions.filter((transaction) => isCurrentMonth(transaction.transactionDate));
  const monthlyIncome = numberOrNull(monthlySummary.monthlyIncome) ?? 0;
  const monthlyExpenses = numberOrNull(monthlySummary.monthlyExpenses) ?? 0;
  const monthlyBalance = numberOrNull(monthlySummary.monthlyBalance) ?? monthlyIncome - monthlyExpenses;
  const reviewCount = countReviewableTransactions(currentMonthTransactions);
  const actions = buildPriorityActions(currentMonthTransactions, reviewCount);
  const analysisStatus = buildAnalysisStatus(currentMonthTransactions, reviewCount);

  if (currentMonthTransactions.length === 0) {
    return {
      title: "Prioridad del mes",
      description: "Aún no hay movimientos de este mes.",
      whyItMatters: "Registra un ingreso o gasto para que AppSueldo pueda decirte cómo va tu flujo mensual.",
      analysisStatus,
      tone: "neutral",
      actions,
    };
  }

  if (monthlyBalance < 0) {
    return {
      title: "Prioridad del mes",
      description: "Tu flujo mensual está en negativo.",
      whyItMatters:
        reviewCount > 0
          ? "Revisa si los gastos corresponden a compras reales, transferencias o movimientos pendientes de ordenar."
          : "Revisa tus últimos movimientos para confirmar que el mes realmente va con más gasto que ingreso.",
      analysisStatus,
      tone: "expense",
      actions,
    };
  }

  if (monthlyIncome === 0 && monthlyExpenses > 0) {
    return {
      title: "Prioridad del mes",
      description: "Hay gastos registrados, pero todavía no hay ingresos del mes.",
      whyItMatters: "Agrega o importa tus ingresos para que el resultado mensual tenga contexto.",
      analysisStatus,
      tone: "amber",
      actions,
    };
  }

  return {
    title: "Prioridad del mes",
    description: reviewCount > 0 ? "El mes va bien, pero hay movimientos por ordenar." : "Este mes vas con flujo positivo.",
    whyItMatters:
      reviewCount > 0
        ? "Ordenar tus movimientos mejora el análisis de gastos y evita conclusiones confusas."
        : "Mantén tus movimientos al día para saber si el sueldo alcanza hasta fin de mes.",
    analysisStatus,
    tone: monthlyBalance > 0 ? "income" : "neutral",
    actions,
  };
}

export function buildCategoryReadiness(
  distribution: CategoryDistributionItem[],
  transactions: Transaction[],
  limit = 5,
): CategoryReadinessViewModel {
  const currentExpenses = transactions.filter(
    (transaction) => transaction.type === "EXPENSE" && isCurrentMonth(transaction.transactionDate),
  );
  const uncategorizedExpenses = currentExpenses.filter((transaction) => !hasTransactionCategory(transaction));
  const totalExpenseAmount = currentExpenses.reduce((sum, transaction) => sum + (numberOrNull(transaction.amount) ?? 0), 0);
  const uncategorizedAmount = uncategorizedExpenses.reduce((sum, transaction) => sum + (numberOrNull(transaction.amount) ?? 0), 0);
  const uncategorizedPercent = totalExpenseAmount > 0 ? Math.round((uncategorizedAmount / totalExpenseAmount) * 100) : 0;
  const items = distribution
    .filter((item) => item.type === "EXPENSE")
    .filter((item) => item.name !== "Sin categoría")
    .sort((first, second) => second.rawAmount - first.rawAmount)
    .slice(0, limit)
    .map((item) => ({
      id: item.id,
      name: item.name,
      amount: formatCurrencyCLP(item.rawAmount),
      percent: item.percent,
      percentLabel: item.percentLabel,
      movementCount: item.movementCount,
      tone: item.tone,
    }));

  if (currentExpenses.length === 0) {
    return {
      title: "Análisis por categoría",
      description: "Cuando tengas gastos ordenados, aquí verás en qué se concentra tu gasto.",
      status: "empty",
      uncategorizedExpenseCount: 0,
      items: [],
    };
  }

  if (items.length === 0 || uncategorizedPercent >= 50) {
    return {
      title: "Análisis por categoría",
      description: "Aún faltan movimientos por ordenar. Cuando tus movimientos tengan categoría, aquí verás en qué se concentra tu gasto.",
      status: "needs_categories",
      uncategorizedExpenseCount: uncategorizedExpenses.length,
      uncategorizedPercentLabel: `${uncategorizedPercent}%`,
      items,
    };
  }

  return {
    title: "Análisis por categoría",
    description: "Gastos del mes. Las transferencias no se incluyen.",
    status: "ready",
    uncategorizedExpenseCount: uncategorizedExpenses.length,
    uncategorizedPercentLabel: uncategorizedPercent > 0 ? `${uncategorizedPercent}%` : undefined,
    items,
  };
}

export function buildPendingActions(
  transactions: Transaction[],
  bankConnections: BankConnection[],
): DashboardActionItem[] {
  const currentMonthTransactions = transactions.filter((transaction) => isCurrentMonth(transaction.transactionDate));
  const uncategorizedCount = countReviewableTransactions(currentMonthTransactions);
  const accountCount = bankConnections.reduce((count, connection) => count + connection.accounts.length, 0);
  const actions: DashboardActionItem[] = [];

  if (uncategorizedCount > 0) {
    actions.push({
      id: "uncategorized",
      title: `Tienes ${uncategorizedCount} ${uncategorizedCount === 1 ? "movimiento sin categoría" : "movimientos sin categoría"}`,
      description: "Ordenarlos mejora las categorías principales y la lectura del mes.",
      ctaLabel: "Revisar movimientos",
      href: "/transactions",
      tone: "amber",
      icon: Tags,
    });
  }

  if (currentMonthTransactions.length > 0) {
    actions.push({
      id: "recent",
      title: "Revisa tus últimos movimientos",
      description: "Confirma que los montos, categorías y orígenes estén correctos.",
      ctaLabel: "Abrir historial",
      href: "/transactions",
      tone: "neutral",
      icon: Clock3,
    });
  }

  if (accountCount === 0) {
    actions.push({
      id: "connect-bank",
      title: "Conecta tu banco para traer movimientos",
      description: "El flujo de conexión ya está disponible en ajustes.",
      ctaLabel: "Conectar banco",
      href: "/settings",
      tone: "transfer",
      icon: Landmark,
    });
  }

  if (actions.length === 0) {
    actions.push({
      id: "all-set",
      title: "No hay acciones urgentes",
      description: "Tu dashboard no tiene movimientos pendientes de revisar con los datos actuales.",
      tone: "income",
      icon: CircleCheck,
    });
  }

  return actions;
}

export function buildSalaryRhythm(monthlySummary: MonthlySummary, referenceDate = new Date()): SalaryRhythmViewModel {
  const monthlyIncome = numberOrNull(monthlySummary.monthlyIncome) ?? 0;
  const monthlyExpenses = numberOrNull(monthlySummary.monthlyExpenses) ?? 0;
  const monthlyBalance = numberOrNull(monthlySummary.monthlyBalance) ?? monthlyIncome - monthlyExpenses;
  const dayOfMonth = referenceDate.getDate();
  const daysInMonth = new Date(referenceDate.getFullYear(), referenceDate.getMonth() + 1, 0).getDate();
  const daysRemaining = Math.max(daysInMonth - dayOfMonth, 0);
  const averageDailyExpense = dayOfMonth > 0 ? monthlyExpenses / dayOfMonth : 0;

  if (monthlyIncome === 0 && monthlyExpenses === 0) {
    return {
      title: "Ritmo del mes",
      description: "Aún no hay datos suficientes para saber si el sueldo alcanza hasta fin de mes.",
      note: "Calculado según tus gastos registrados este mes.",
      tone: "neutral",
      facts: [
        { label: "Días restantes", value: String(daysRemaining) },
        { label: "Entró", value: formatCurrencyCLP(monthlyIncome) },
        { label: "Gastado", value: formatCurrencyCLP(monthlyExpenses) },
        { label: "Promedio diario gastado", value: "$0" },
      ],
    };
  }

  if (monthlyBalance < 0) {
    return {
      title: "Ritmo del mes",
      description: `Quedan ${daysRemaining} días del mes. Con el ritmo actual, estás gastando más de lo que entra.`,
      note: "Calculado según tus gastos registrados este mes.",
      tone: "neutral",
      facts: [
        { label: "Días restantes", value: String(daysRemaining) },
        { label: "Entró", value: formatCurrencyCLP(monthlyIncome) },
        { label: "Gastado", value: formatCurrencyCLP(monthlyExpenses) },
        { label: "Promedio diario gastado", value: formatCurrencyCLP(averageDailyExpense) },
      ],
    };
  }

  return {
    title: "Ritmo del mes",
    description: `Este mes vas con flujo positivo. Quedan ${daysRemaining} días para seguir cuidando el sueldo.`,
    note: "Calculado según tus gastos registrados este mes.",
    tone: "neutral",
    facts: [
      { label: "Días restantes", value: String(daysRemaining) },
      { label: "Entró", value: formatCurrencyCLP(monthlyIncome) },
      { label: "Gastado", value: formatCurrencyCLP(monthlyExpenses) },
      { label: "Promedio diario gastado", value: formatCurrencyCLP(averageDailyExpense) },
    ],
  };
}

export function buildRecentTransactions(transactions: Transaction[], limit = 5): TransactionListItem[] {
  return [...transactions]
    .sort((first, second) => {
      const firstTime = parseTransactionDate(first.transactionDate)?.getTime() ?? 0;
      const secondTime = parseTransactionDate(second.transactionDate)?.getTime() ?? 0;
      return secondTime - firstTime;
    })
    .slice(0, limit)
    .map((transaction) => {
      const typeLabel = getUserFriendlyTransactionType(transaction.type);
      const categoryName = transaction.category?.name ?? transaction.categoryName ?? "Sin categoría";
      const bankAccountName = transaction.bankAccount?.name ?? sourceLabel(transaction.source);
      const review = reviewStatusForTransaction(transaction);

      return {
        id: transaction.id,
        description: transaction.description || typeLabel,
        category: categoryName,
        amount: signedAmount(numberOrNull(transaction.amount) ?? 0, transaction.type),
        date: formatShortDate(transaction.transactionDate),
        typeLabel,
        type: transaction.type,
        source: transaction.source,
        sourceLabel: sourceLabel(transaction.source),
        currency: transaction.currency || "CLP",
        bankAccountName,
        reviewStatus: review.label,
        reviewTone: review.tone,
        icon: iconForType(transaction.type),
        tone: toneForType(transaction.type),
      };
    });
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
      description: `${topExpense.name} concentra ${topExpense.percentLabel} de tus gastos registrados este mes.`,
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

function countReviewableTransactions(transactions: Transaction[]) {
  return transactions.filter((transaction) => transaction.type !== "TRANSFER" && !hasTransactionCategory(transaction)).length;
}

function buildAnalysisStatus(transactions: Transaction[], reviewCount: number) {
  if (transactions.length === 0) {
    return {
      label: "Estado del análisis",
      description: "Aún faltan movimientos del mes para construir una lectura útil.",
      tone: "neutral" as const,
    };
  }

  if (reviewCount > 0) {
    return {
      label: "Estado del análisis",
      description: `Hay ${reviewCount} ${
        reviewCount === 1 ? "movimiento por revisar" : "movimientos por revisar"
      }. El resumen general ya es útil, pero las categorías serán más precisas cuando los movimientos estén ordenados.`,
      tone: "amber" as const,
    };
  }

  return {
    label: "Estado del análisis",
    description: "El resumen general usa los movimientos disponibles y las categorías ordenadas hasta ahora.",
    tone: "income" as const,
  };
}

function hasTransactionCategory(transaction: Transaction) {
  const categoryId = transaction.category?.id ?? transaction.categoryId;
  const categoryName = transaction.category?.name ?? transaction.categoryName;
  return (
    (categoryId !== null && categoryId !== undefined) ||
    (typeof categoryName === "string" && categoryName.trim().length > 0 && categoryName.trim() !== "Sin categoría")
  );
}

function buildPriorityActions(transactions: Transaction[], reviewCount: number): DashboardActionItem[] {
  const actions: DashboardActionItem[] = [];

  if (reviewCount > 0) {
    actions.push({
      id: "review-uncategorized",
      title: `Ordena ${reviewCount} ${reviewCount === 1 ? "movimiento" : "movimientos"}`,
      description: "Revisa categoría, tipo y origen para mejorar el análisis del mes.",
      ctaLabel: "Revisar movimientos",
      href: "/transactions",
      tone: "amber",
      icon: Tags,
    });
  }

  if (transactions.length > 0) {
    actions.push({
      id: "open-history",
      title: "Ver historial del mes",
      description: "Confirma qué movimientos explican el resultado actual.",
      ctaLabel: "Ver historial",
      href: "/transactions",
      tone: "neutral",
      icon: Clock3,
    });
  }

  actions.push({
    id: "add-movement",
    title: "Agregar un movimiento",
    description: "Registra un ingreso o gasto que falte en el mes.",
    ctaLabel: "Agregar movimiento",
    href: "/transactions/new",
    tone: "income",
    icon: CalendarDays,
  });

  return actions.slice(0, 3);
}

function reviewStatusForTransaction(transaction: Transaction): { label: string; tone: Tone } {
  if (transaction.type === "TRANSFER") {
    return { label: "No es gasto", tone: "transfer" };
  }

  if (!hasTransactionCategory(transaction)) {
    return { label: "Sin categoría", tone: "amber" };
  }

  if (transaction.source === "FINTOC") {
    return { label: "Importado", tone: "neutral" };
  }

  if (transaction.source === "CSV_IMPORT") {
    return { label: "Importado CSV", tone: "neutral" };
  }

  return { label: "Registrado", tone: "income" };
}

function iconForType(type: TransactionType) {
  if (type === "INCOME") {
    return ArrowUpCircle;
  }
  if (type === "TRANSFER") {
    return ArrowRightLeft;
  }
  return ArrowDownCircle;
}

function sourceLabel(source: Transaction["source"]) {
  if (source === "FINTOC") {
    return "Banco conectado";
  }
  if (source === "CSV_IMPORT") {
    return "Importado por CSV";
  }
  return "Registro manual";
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
