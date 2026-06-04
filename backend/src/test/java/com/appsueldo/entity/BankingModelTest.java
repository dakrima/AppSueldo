package com.appsueldo.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BankingModelTest {

    @Test
    void transactionDefaultsToClpAndManualSource() {
        Transaction transaction = new Transaction();

        assertThat(transaction.getCurrency()).isEqualTo("CLP");
        assertThat(transaction.getSource()).isEqualTo(TransactionSource.MANUAL);
    }

    @Test
    void transactionRejectsNonPositiveAmount() {
        Transaction transaction = new Transaction();

        assertThatThrownBy(() -> transaction.setAmount(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Transaction amount must be positive.");
    }

    @Test
    void classificationCanBeAssociatedWithTransaction() {
        User user = new User();
        Category category = new Category();
        Transaction transaction = new Transaction();
        TransactionClassification classification = new TransactionClassification();

        classification.setUser(user);
        classification.setTransaction(transaction);
        classification.setCategory(category);
        classification.setMethod(ClassificationMethod.MANUAL);
        classification.setReason("User selected this category.");

        assertThat(classification.getUser()).isSameAs(user);
        assertThat(classification.getTransaction()).isSameAs(transaction);
        assertThat(classification.getCategory()).isSameAs(category);
        assertThat(classification.getMethod()).isEqualTo(ClassificationMethod.MANUAL);
        assertThat(classification.getReason()).isEqualTo("User selected this category.");
    }
}
