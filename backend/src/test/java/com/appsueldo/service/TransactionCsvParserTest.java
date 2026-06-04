package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TransactionCsvParserTest {

    private final TransactionCsvParser parser = new TransactionCsvParser();

    @Test
    void parsesValidRowsWithSemicolonDelimiterAndInfersExpenseFromNegativeAmount() {
        String csv = """
            fecha;monto;descripcion;moneda
            2026-06-01;-12.990;"Supermercado, compra";CLP
            04/06/2026;850000;Sueldo mensual;
            """;

        TransactionCsvParser.ParseResult result = parser.parse(input(csv), account());

        assertThat(result.validRows()).hasSize(2);
        assertThat(result.invalidRows()).isEmpty();
        assertThat(result.validRows().get(0).amount()).isEqualByComparingTo("12990");
        assertThat(result.validRows().get(0).type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.validRows().get(0).description()).isEqualTo("Supermercado, compra");
        assertThat(result.validRows().get(1).currency()).isEqualTo("CLP");
        assertThat(result.validRows().get(1).type()).isEqualTo(TransactionType.INCOME);
        assertThat(result.validRows().get(0).externalId()).startsWith("csv:");
    }

    @Test
    void reportsInvalidRowsWithoutRejectingWholeFile() {
        String csv = """
            date,amount,description,currency,type
            2026-06-01,0,Cero,CLP,expense
            not-a-date,1000,Cafe,CLP,expense
            2026-06-02,2500,,CLP,expense
            2026-06-03,3500,Cafe,USD,expense
            """;

        TransactionCsvParser.ParseResult result = parser.parse(input(csv), account());

        assertThat(result.validRows()).hasSize(1);
        assertThat(result.invalidRows()).hasSize(3);
        assertThat(result.totalRows()).isEqualTo(4);
        assertThat(result.validRows().get(0).currency()).isEqualTo("USD");
    }

    @Test
    void rejectsCsvWithoutRequiredHeaders() {
        String csv = """
            fecha,detalle
            2026-06-01,Cafe
            """;

        assertThatThrownBy(() -> parser.parse(input(csv), account()))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("El CSV debe incluir columnas de fecha, monto y descripcion.");
    }

    private ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    private BankAccount account() {
        BankAccount account = new BankAccount();
        setId(account, 10L);
        account.setCurrency("CLP");
        return account;
    }

    private void setId(BankAccount account, Long id) {
        try {
            var field = BankAccount.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set bank account id for test.", exception);
        }
    }
}
