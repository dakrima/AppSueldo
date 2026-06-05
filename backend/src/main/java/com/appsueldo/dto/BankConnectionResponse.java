package com.appsueldo.dto;

import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import java.util.List;

public record BankConnectionResponse(
    Long id,
    BankProvider provider,
    String institutionName,
    BankConnectionStatus status,
    List<BankAccountSummaryDto> accounts,
    Integer importedTransactionsCount,
    Integer skippedTransactionsCount,
    String syncStatus
) {
    public static BankConnectionResponse from(
        BankConnection bankConnection,
        List<BankAccountSummaryDto> accounts
    ) {
        return from(bankConnection, accounts, null, null, null);
    }

    public static BankConnectionResponse from(
        BankConnection bankConnection,
        List<BankAccountSummaryDto> accounts,
        Integer importedTransactionsCount,
        Integer skippedTransactionsCount,
        String syncStatus
    ) {
        return new BankConnectionResponse(
            bankConnection.getId(),
            bankConnection.getProvider(),
            bankConnection.getInstitutionName(),
            bankConnection.getStatus(),
            accounts,
            importedTransactionsCount,
            skippedTransactionsCount,
            syncStatus
        );
    }
}
