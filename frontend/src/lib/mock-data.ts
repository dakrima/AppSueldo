import { Banknote, PiggyBank, TrendingDown, Wallet } from "lucide-react";

export type TransactionType = "INCOME" | "EXPENSE" | "TRANSFER";

export type Transaction = {
  id: string;
  description: string;
  category: string;
  amount: string;
  date: string;
  type: TransactionType;
};

export const monthlySummary = [
  {
    label: "Ingresos del mes",
    value: "$2.200.000",
    helper: "Sueldo y otros ingresos manuales",
    tone: "emerald" as const,
    icon: Banknote,
  },
  {
    label: "Gastos del mes",
    value: "$954.100",
    helper: "Egresos categorizados del periodo",
    tone: "amber" as const,
    icon: TrendingDown,
  },
  {
    label: "Ahorro estimado",
    value: "$420.000",
    helper: "Meta simple calculada desde mocks",
    tone: "emerald" as const,
    icon: PiggyBank,
  },
  {
    label: "Saldo disponible",
    value: "$1.245.900",
    helper: "Balance operativo del mes",
    tone: "slate" as const,
    icon: Wallet,
  },
];

export const recentTransactions: Transaction[] = [
  { id: "tx-1", description: "Sueldo mensual", category: "Ingresos", amount: "+$2.100.000", date: "02 jun", type: "INCOME" },
  { id: "tx-2", description: "Supermercado", category: "Alimentacion", amount: "-$86.400", date: "01 jun", type: "EXPENSE" },
  { id: "tx-3", description: "Arriendo", category: "Vivienda", amount: "-$520.000", date: "31 may", type: "EXPENSE" },
  { id: "tx-4", description: "Transferencia ahorro", category: "Ahorro", amount: "-$150.000", date: "30 may", type: "TRANSFER" },
  { id: "tx-5", description: "Transporte", category: "Movilidad", amount: "-$24.500", date: "29 may", type: "EXPENSE" },
];

export const categories = [
  { name: "Alimentacion", type: "EXPENSE", color: "#10b981", percent: 32 },
  { name: "Vivienda", type: "EXPENSE", color: "#f59e0b", percent: 58 },
  { name: "Movilidad", type: "EXPENSE", color: "#64748b", percent: 18 },
  { name: "Ingresos", type: "INCOME", color: "#059669", percent: 100 },
];
