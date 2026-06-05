export type TransactionType = "INCOME" | "EXPENSE" | "TRANSFER";

export type TransactionSource = "MANUAL" | "FINTOC" | "CSV_IMPORT";

export type ImportBatchStatus = "PROCESSING" | "COMPLETED" | "FAILED";

export type CategoryType = "INCOME" | "EXPENSE";

export type BankProvider = "MANUAL" | "FINTOC";

export type BankConnectionStatus = "ACTIVE" | "INACTIVE" | "ERROR" | "PENDING";

export type Category = {
  id: number;
  name: string;
  type: CategoryType;
  color: string | null;
  icon: string | null;
};

export type BankAccount = {
  id: number;
  name: string;
  accountType: string | null;
  currency: string;
  balance: number | null;
};

export type BankConnection = {
  id: number;
  provider: BankProvider;
  institutionName: string | null;
  status: BankConnectionStatus;
  accounts: BankAccount[];
};

export type CreateFintocLinkIntentResponse = {
  provider: BankProvider;
  publicKey: string;
  widgetToken: string;
  country: string;
  product: string;
};

export type Transaction = {
  id: number;
  categoryId: number | null;
  categoryName: string | null;
  category: Category | null;
  bankAccountId: number | null;
  bankAccount: BankAccount | null;
  amount: number;
  currency: string;
  description: string;
  transactionDate: string;
  type: TransactionType;
  source: TransactionSource;
  notes: string | null;
};

export type MonthlySummary = {
  monthlyIncome: number;
  monthlyExpenses: number;
  estimatedSavings: number;
  availableBalance: number;
  monthlyBalance: number;
  transactionCount: number;
};

export type ImportBatch = {
  id: number;
  status: ImportBatchStatus;
  originalFilename: string;
  importSource: string;
  bankAccountId: number;
  bankAccountName: string;
  totalRows: number;
  createdCount: number;
  skippedCount: number;
  invalidCount: number;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string;
};
