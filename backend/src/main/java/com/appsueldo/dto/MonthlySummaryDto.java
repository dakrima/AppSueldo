package com.appsueldo.dto;

import java.math.BigDecimal;

public record MonthlySummaryDto(
    BigDecimal monthlyIncome,
    BigDecimal monthlyExpenses,
    BigDecimal estimatedSavings,
    BigDecimal availableBalance
) {
}
