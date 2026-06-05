package com.appsueldo.dto;

public record BankConnectionSyncResponse(
    String status,
    boolean requiresMfa,
    String widgetToken,
    Integer importedTransactionsCount,
    Integer skippedTransactionsCount,
    String syncStatus
) {
    public static BankConnectionSyncResponse mfaRequired(String widgetToken) {
        return new BankConnectionSyncResponse("REQUIRES_MFA", true, widgetToken, 0, 0, "PENDING");
    }

    public static BankConnectionSyncResponse fromSync(
        String status,
        Integer importedTransactionsCount,
        Integer skippedTransactionsCount,
        String syncStatus
    ) {
        return new BankConnectionSyncResponse(
            status,
            false,
            null,
            importedTransactionsCount,
            skippedTransactionsCount,
            syncStatus
        );
    }

    @Override
    public String toString() {
        return "BankConnectionSyncResponse[status=" + status
            + ", requiresMfa=" + requiresMfa
            + ", widgetToken=" + (widgetToken == null || widgetToken.isBlank() ? "" : "****")
            + ", importedTransactionsCount=" + importedTransactionsCount
            + ", skippedTransactionsCount=" + skippedTransactionsCount
            + ", syncStatus=" + syncStatus + "]";
    }
}
