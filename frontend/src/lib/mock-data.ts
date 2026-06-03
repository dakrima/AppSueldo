import {
  Banknote,
  Bus,
  CalendarClock,
  CircleDollarSign,
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

export type TransactionType = "INCOME" | "EXPENSE" | "TRANSFER";
export type CategoryType = "INCOME" | "EXPENSE";
export type Tone = "income" | "expense" | "transfer" | "amber" | "neutral";

export type Transaction = {
  id: string;
  description: string;
  category: string;
  amount: string;
  date: string;
  type: TransactionType;
  icon: LucideIcon;
  tone: Tone;
};

export type Category = {
  id: string;
  name: string;
  type: CategoryType;
  amount: string;
  percent: number;
  icon: LucideIcon;
  tone: Tone;
};

export const mainBalance = {
  month: "Junio 2026",
  label: "Te queda para el mes",
  value: "$317.700",
  helper: "Considerando tus gastos registrados y pagos fijos pendientes.",
};

export const monthlySummary = [
  {
    label: "Ingresos",
    value: "+$850.000",
    helper: "Sueldo y entradas manuales",
    tone: "income" as const,
    icon: Banknote,
  },
  {
    label: "Gastos",
    value: "-$412.300",
    helper: "Gastos registrados del mes",
    tone: "expense" as const,
    icon: ReceiptText,
  },
  {
    label: "Ahorro estimado",
    value: "$120.000",
    helper: "Proyección simple del periodo",
    tone: "neutral" as const,
    icon: PiggyBank,
  },
  {
    label: "Fijos restantes",
    value: "$95.000",
    helper: "Pagos pendientes por registrar",
    tone: "amber" as const,
    icon: CalendarClock,
  },
];

export const recentTransactions: Transaction[] = [
  {
    id: "tx-1",
    description: "Sueldo mensual",
    category: "Ingresos",
    amount: "+$850.000",
    date: "02 jun",
    type: "INCOME",
    icon: Banknote,
    tone: "income",
  },
  {
    id: "tx-2",
    description: "Supermercado",
    category: "Alimentación",
    amount: "-$46.800",
    date: "05 jun",
    type: "EXPENSE",
    icon: ShoppingCart,
    tone: "expense",
  },
  {
    id: "tx-3",
    description: "Metro / transporte",
    category: "Movilidad",
    amount: "-$12.000",
    date: "08 jun",
    type: "EXPENSE",
    icon: Bus,
    tone: "amber",
  },
  {
    id: "tx-4",
    description: "Transferencia ahorro",
    category: "Ahorro",
    amount: "-$80.000",
    date: "10 jun",
    type: "TRANSFER",
    icon: PiggyBank,
    tone: "transfer",
  },
  {
    id: "tx-5",
    description: "Café con amigos",
    category: "Salidas",
    amount: "-$9.500",
    date: "12 jun",
    type: "EXPENSE",
    icon: Coffee,
    tone: "neutral",
  },
];

export const transactions: Transaction[] = [
  ...recentTransactions,
  {
    id: "tx-6",
    description: "Suscripción streaming",
    category: "Suscripciones",
    amount: "-$28.900",
    date: "13 jun",
    type: "EXPENSE",
    icon: Clapperboard,
    tone: "expense",
  },
  {
    id: "tx-7",
    description: "Entrada cine",
    category: "Salidas",
    amount: "-$15.000",
    date: "15 jun",
    type: "EXPENSE",
    icon: Ticket,
    tone: "expense",
  },
];

export const categories: Category[] = [
  {
    id: "cat-income",
    name: "Sueldo",
    type: "INCOME",
    amount: "+$850.000",
    percent: 100,
    icon: Banknote,
    tone: "income",
  },
  {
    id: "cat-home",
    name: "Vivienda",
    type: "EXPENSE",
    amount: "-$250.000",
    percent: 62,
    icon: Home,
    tone: "expense",
  },
  {
    id: "cat-food",
    name: "Alimentación",
    type: "EXPENSE",
    amount: "-$180.500",
    percent: 46,
    icon: ForkKnife,
    tone: "expense",
  },
  {
    id: "cat-mobility",
    name: "Movilidad",
    type: "EXPENSE",
    amount: "-$45.000",
    percent: 22,
    icon: Bus,
    tone: "amber",
  },
  {
    id: "cat-outings",
    name: "Salidas",
    type: "EXPENSE",
    amount: "-$35.000",
    percent: 14,
    icon: Ticket,
    tone: "expense",
  },
  {
    id: "cat-subscriptions",
    name: "Suscripciones",
    type: "EXPENSE",
    amount: "-$28.900",
    percent: 9,
    icon: Clapperboard,
    tone: "expense",
  },
  {
    id: "cat-health",
    name: "Salud",
    type: "EXPENSE",
    amount: "-$15.000",
    percent: 5,
    icon: HeartPulse,
    tone: "expense",
  },
];

export const categoryBreakdown = [
  { name: "Alimentación", amount: "$180.500", percent: 72, tone: "expense" as const },
  { name: "Vivienda", amount: "$250.000", percent: 100, tone: "neutral" as const },
  { name: "Movilidad", amount: "$45.000", percent: 38, tone: "income" as const },
];

export const insights = [
  {
    title: "Vas bien",
    description: "Tus gastos fijos principales ya están cubiertos.",
    tone: "income" as const,
  },
  {
    title: "Ojo con alimentación",
    description: "Gastaste un 12% más en comida que el mes pasado, pero sigues dentro del margen.",
    tone: "amber" as const,
  },
];

export const periodSummary = {
  income: "+$850.000",
  expenses: "-$138.800",
  note: "Incluye gastos y transferencias salientes.",
};

export const newTransactionCategories = [
  { name: "Alimentación", icon: ForkKnife },
  { name: "Vivienda", icon: Home },
  { name: "Movilidad", icon: Bus },
  { name: "Ocio", icon: Ticket },
  { name: "Ahorro", icon: PiggyBank },
];

export const emptyCategory = {
  name: "Crear nueva",
  description: "Personaliza tus gastos",
  icon: CircleDollarSign,
};
