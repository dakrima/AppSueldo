package com.appsueldo.service.fintoc;

import static org.assertj.core.api.Assertions.assertThat;

import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class FintocMovementMapperTest {

    private final FintocMovementMapper mapper = new FintocMovementMapper();

    @Test
    void positiveFintocAmountBecomesIncomeWithPositiveAmount() {
        FintocMovementResponse movement = movement("mov_income", 59400L, "CLP");

        FintocTransactionDraft draft = mapper.toTransactionDraft(movement);

        assertThat(draft.type()).isEqualTo(TransactionType.INCOME);
        assertThat(draft.amount()).isEqualByComparingTo("59400");
        assertThat(draft.source()).isEqualTo(TransactionSource.FINTOC);
        assertThat(draft.externalId()).isEqualTo("mov_income");
    }

    @Test
    void negativeFintocAmountBecomesExpenseWithPositiveAmount() {
        FintocMovementResponse movement = movement("mov_expense", -12990L, "CLP");

        FintocTransactionDraft draft = mapper.toTransactionDraft(movement);

        assertThat(draft.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(draft.amount()).isEqualByComparingTo("12990");
        assertThat(draft.source()).isEqualTo(TransactionSource.FINTOC);
        assertThat(draft.externalId()).isEqualTo("mov_expense");
    }

    @Test
    void preservesCurrencyAndUsesPostDate() {
        FintocMovementResponse movement = movement("mov_usd", -150L, "usd");

        FintocTransactionDraft draft = mapper.toTransactionDraft(movement);

        assertThat(draft.currency()).isEqualTo("USD");
        assertThat(draft.transactionDate()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(draft.description()).isEqualTo("Supermercado");
    }

    private FintocMovementResponse movement(String id, Long amount, String currency) {
        return new FintocMovementResponse(
            id,
            "movement",
            amount,
            OffsetDateTime.parse("2026-05-25T12:30:00Z"),
            "Supermercado",
            OffsetDateTime.parse("2026-05-24T12:30:00Z"),
            currency,
            "ref_123",
            "purchase",
            false,
            "posted",
            null,
            null,
            null
        );
    }
}
