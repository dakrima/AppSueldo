package com.appsueldo.service;

import com.appsueldo.entity.BankAccount;
import java.util.List;

public record FintocSyncResult(
    int importedTransactionsCount,
    int skippedTransactionsCount,
    String syncStatus,
    List<BankAccount> accounts
) {
    public static FintocSyncResult completed(
        int importedTransactionsCount,
        int skippedTransactionsCount,
        List<BankAccount> accounts
    ) {
        return new FintocSyncResult(importedTransactionsCount, skippedTransactionsCount, "COMPLETED", accounts);
    }

    public static FintocSyncResult failed() {
        return new FintocSyncResult(0, 0, "ERROR", List.of());
    }
}
