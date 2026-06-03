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
    BigDecimal amount,
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
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getType(),
            transaction.getSource(),
            transaction.getNotes()
        );
    }
}
