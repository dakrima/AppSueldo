package com.appsueldo.service;

import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.exception.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TransactionCsvParser {

    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/uuuu"),
        DateTimeFormatter.ofPattern("dd-MM-uuuu"),
        new DateTimeFormatterBuilder()
            .appendPattern("d/M/uuuu")
            .parseDefaulting(ChronoField.ERA, 1)
            .toFormatter()
    );

    public ParseResult parse(InputStream inputStream, BankAccount bankAccount) {
        List<String> lines = readLines(inputStream);
        if (lines.isEmpty()) {
            throw new BadRequestException("El archivo CSV esta vacio.");
        }

        char delimiter = detectDelimiter(lines.get(0));
        List<String> headers = parseLine(lines.get(0), delimiter);
        HeaderIndexes indexes = HeaderIndexes.from(headers);
        List<ValidRow> validRows = new ArrayList<>();
        List<InvalidRow> invalidRows = new ArrayList<>();

        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) {
                continue;
            }
            int rowNumber = index + 1;
            List<String> columns = parseLine(line, delimiter);
            RowParseOutcome outcome = parseRow(rowNumber, columns, indexes, bankAccount);
            if (outcome.validRow() != null) {
                validRows.add(outcome.validRow());
            } else {
                invalidRows.add(new InvalidRow(rowNumber, outcome.reason()));
            }
        }

        return new ParseResult(validRows, invalidRows);
    }

    private RowParseOutcome parseRow(
        int rowNumber,
        List<String> columns,
        HeaderIndexes indexes,
        BankAccount bankAccount
    ) {
        try {
            LocalDate date = parseDate(valueAt(columns, indexes.dateIndex()));
            ParsedAmount parsedAmount = parseAmount(valueAt(columns, indexes.amountIndex()));
            String description = parseDescription(valueAt(columns, indexes.descriptionIndex()));
            String currency = parseCurrency(valueAt(columns, indexes.currencyIndex()), bankAccount);
            TransactionType type = parseType(valueAt(columns, indexes.typeIndex()), parsedAmount.negative());
            BigDecimal amount = parsedAmount.amount().abs();
            String externalId = externalId(bankAccount, date, amount, currency, description, type);
            return new RowParseOutcome(
                new ValidRow(rowNumber, date, amount, description, currency, type, externalId),
                null
            );
        } catch (IllegalArgumentException exception) {
            return new RowParseOutcome(null, exception.getMessage());
        }
    }

    private List<String> readLines(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                .map(line -> line.startsWith("\uFEFF") ? line.substring(1) : line)
                .toList();
        } catch (IOException exception) {
            throw new BadRequestException("No pudimos leer el archivo CSV.");
        }
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(value -> value == ';').count();
        long commas = headerLine.chars().filter(value -> value == ',').count();
        return semicolons > commas ? ';' : ',';
    }

    private List<String> parseLine(String line, char delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == delimiter && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        values.add(current.toString().trim());
        return values;
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        String value = rawDate.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next supported format.
            }
        }
        throw new IllegalArgumentException("La fecha no tiene un formato valido.");
    }

    private ParsedAmount parseAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new IllegalArgumentException("El monto es obligatorio.");
        }

        String value = rawAmount.trim()
            .replace("$", "")
            .replace("CLP", "")
            .replace("USD", "")
            .replace(" ", "");
        boolean negative = value.startsWith("-") || (value.startsWith("(") && value.endsWith(")"));
        value = value.replace("(", "").replace(")", "").replace("+", "").replace("-", "");

        if (value.isBlank()) {
            throw new IllegalArgumentException("El monto no tiene un formato valido.");
        }

        String normalized = normalizeDecimalNumber(value);
        try {
            BigDecimal amount = new BigDecimal(normalized);
            if (amount.signum() == 0) {
                throw new IllegalArgumentException("El monto debe ser distinto de cero.");
            }
            return new ParsedAmount(amount.abs(), negative);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El monto no tiene un formato valido.");
        }
    }

    private String normalizeDecimalNumber(String value) {
        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            char decimalSeparator = lastComma > lastDot ? ',' : '.';
            char groupingSeparator = decimalSeparator == ',' ? '.' : ',';
            return value.replace(String.valueOf(groupingSeparator), "").replace(decimalSeparator, '.');
        }

        if (lastComma >= 0) {
            return normalizeSingleSeparator(value, ',');
        }

        if (lastDot >= 0) {
            return normalizeSingleSeparator(value, '.');
        }

        return value;
    }

    private String normalizeSingleSeparator(String value, char separator) {
        int lastSeparator = value.lastIndexOf(separator);
        int decimals = value.length() - lastSeparator - 1;
        if (decimals > 0 && decimals <= 2) {
            return value.replace(separator, '.');
        }
        return value.replace(String.valueOf(separator), "");
    }

    private String parseDescription(String rawDescription) {
        if (rawDescription == null || rawDescription.isBlank()) {
            throw new IllegalArgumentException("La descripcion es obligatoria.");
        }

        String description = rawDescription.trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("La descripcion no puede superar 255 caracteres.");
        }
        return description;
    }

    private String parseCurrency(String rawCurrency, BankAccount bankAccount) {
        String currency = rawCurrency == null || rawCurrency.isBlank()
            ? bankAccount.getCurrency()
            : rawCurrency.trim().toUpperCase(Locale.ROOT);

        if (currency == null || currency.isBlank()) {
            currency = BankAccount.DEFAULT_CURRENCY;
        }

        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("La moneda debe tener codigo ISO de 3 letras.");
        }
        return currency;
    }

    private TransactionType parseType(String rawType, boolean amountWasNegative) {
        if (rawType == null || rawType.isBlank()) {
            return amountWasNegative ? TransactionType.EXPENSE : TransactionType.INCOME;
        }

        String normalizedType = normalizeHeader(rawType);
        return switch (normalizedType) {
            case "income", "ingreso", "entrada" -> TransactionType.INCOME;
            case "expense", "gasto", "egreso", "salida" -> TransactionType.EXPENSE;
            case "transfer", "transferencia" -> TransactionType.TRANSFER;
            default -> throw new IllegalArgumentException("El tipo de movimiento no es valido.");
        };
    }

    private String externalId(
        BankAccount bankAccount,
        LocalDate date,
        BigDecimal amount,
        String currency,
        String description,
        TransactionType type
    ) {
        String canonical = bankAccount.getId() + "|" + date + "|" + amount.stripTrailingZeros().toPlainString()
            + "|" + currency + "|" + description.trim().toLowerCase(Locale.ROOT) + "|" + type;
        return "csv:" + sha256(canonical);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    private String valueAt(List<String> columns, int index) {
        if (index < 0 || index >= columns.size()) {
            return null;
        }
        return columns.get(index);
    }

    private static String normalizeHeader(String header) {
        return header.trim()
            .toLowerCase(Locale.ROOT)
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace(" ", "_");
    }

    public record ParseResult(List<ValidRow> validRows, List<InvalidRow> invalidRows) {
        public int totalRows() {
            return validRows.size() + invalidRows.size();
        }
    }

    public record ValidRow(
        int rowNumber,
        LocalDate transactionDate,
        BigDecimal amount,
        String description,
        String currency,
        TransactionType type,
        String externalId
    ) {
    }

    public record InvalidRow(int rowNumber, String reason) {
    }

    private record RowParseOutcome(ValidRow validRow, String reason) {
    }

    private record ParsedAmount(BigDecimal amount, boolean negative) {
    }

    private record HeaderIndexes(int dateIndex, int amountIndex, int descriptionIndex, int currencyIndex, int typeIndex) {
        private static HeaderIndexes from(List<String> headers) {
            Map<String, Integer> indexes = new HashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                indexes.put(normalizeHeader(headers.get(index)), index);
            }

            return new HeaderIndexes(
                requiredIndex(indexes, "fecha", "date", "transaction_date"),
                requiredIndex(indexes, "monto", "amount"),
                requiredIndex(indexes, "descripcion", "description", "detalle"),
                optionalIndex(indexes, "moneda", "currency"),
                optionalIndex(indexes, "tipo", "type")
            );
        }

        private static int requiredIndex(Map<String, Integer> indexes, String... names) {
            int index = optionalIndex(indexes, names);
            if (index < 0) {
                throw new BadRequestException("El CSV debe incluir columnas de fecha, monto y descripcion.");
            }
            return index;
        }

        private static int optionalIndex(Map<String, Integer> indexes, String... names) {
            for (String name : names) {
                Integer index = indexes.get(name);
                if (index != null) {
                    return index;
                }
            }
            return -1;
        }
    }
}
