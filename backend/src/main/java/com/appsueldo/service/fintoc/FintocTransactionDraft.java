package com.appsueldo.service.fintoc;

import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FintocTransactionDraft(
    BigDecimal amount,
    TransactionType type,
    TransactionSource source,
    String externalId,
    String currency,
    String description,
    LocalDate transactionDate
) {
}
