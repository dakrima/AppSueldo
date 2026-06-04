package com.appsueldo.dto;

import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
    Long id,
    Long categoryId,
    String categoryName,
    CategoryResponse category,
    Long bankAccountId,
    BankAccountSummaryDto bankAccount,
    BigDecimal amount,
    String currency,
    String description,
    LocalDate transactionDate,
    TransactionType type,
    TransactionSource source,
    String notes
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getCategory() == null ? null : transaction.getCategory().getId(),
            transaction.getCategory() == null ? null : transaction.getCategory().getName(),
            transaction.getCategory() == null ? null : CategoryResponse.from(transaction.getCategory()),
            transaction.getBankAccount() == null ? null : transaction.getBankAccount().getId(),
            transaction.getBankAccount() == null ? null : BankAccountSummaryDto.from(transaction.getBankAccount()),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getType(),
            transaction.getSource(),
            transaction.getNotes()
        );
    }
}
