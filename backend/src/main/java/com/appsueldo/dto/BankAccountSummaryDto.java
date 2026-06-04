package com.appsueldo.dto;

import com.appsueldo.entity.BankAccount;
import java.math.BigDecimal;

public record BankAccountSummaryDto(
    Long id,
    String name,
    String accountType,
    String currency,
    BigDecimal balance
) {
    public static BankAccountSummaryDto from(BankAccount bankAccount) {
        return new BankAccountSummaryDto(
            bankAccount.getId(),
            bankAccount.getName(),
            bankAccount.getAccountType(),
            bankAccount.getCurrency(),
            bankAccount.getBalance()
        );
    }
}
