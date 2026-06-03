package com.appsueldo.dto;

import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
    Long categoryId,
    @NotNull @Positive BigDecimal amount,
    @NotBlank @Size(max = 255) String description,
    @NotNull LocalDate transactionDate,
    @NotNull TransactionType type,
    TransactionSource source,
    String notes
) {
}
