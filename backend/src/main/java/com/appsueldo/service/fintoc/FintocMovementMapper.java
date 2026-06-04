package com.appsueldo.service.fintoc;

import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class FintocMovementMapper {

    public FintocTransactionDraft toTransactionDraft(FintocMovementResponse movement) {
        if (movement == null) {
            throw new IllegalArgumentException("Fintoc movement is required.");
        }
        if (movement.id() == null || movement.id().isBlank()) {
            throw new IllegalArgumentException("Fintoc movement id is required.");
        }
        if (movement.amount() == null || movement.amount() == 0) {
            throw new IllegalArgumentException("Fintoc movement amount must not be zero.");
        }
        if (movement.description() == null || movement.description().isBlank()) {
            throw new IllegalArgumentException("Fintoc movement description is required.");
        }

        TransactionType type = movement.amount() > 0 ? TransactionType.INCOME : TransactionType.EXPENSE;

        // TODO: Confirm Fintoc amount exponent per currency before supporting non-CLP currencies.
        BigDecimal normalizedAmount = BigDecimal.valueOf(movement.amount()).abs();

        return new FintocTransactionDraft(
            normalizedAmount,
            type,
            TransactionSource.FINTOC,
            movement.id(),
            normalizeCurrency(movement.currency()),
            movement.description().trim(),
            resolveTransactionDate(movement)
        );
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return BankAccount.DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private LocalDate resolveTransactionDate(FintocMovementResponse movement) {
        OffsetDateTime date = movement.postDate() != null
            ? movement.postDate()
            : movement.transactionDate();

        if (date == null) {
            throw new IllegalArgumentException("Fintoc movement transaction date is required.");
        }
        return date.toLocalDate();
    }
}
