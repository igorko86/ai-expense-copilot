package com.copilot.backend.expense;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedExpenseDto(
        @JsonPropertyDescription("Short description or merchant name, e.g. Silpo, Uklon, Netflix")
        String description,

        @JsonPropertyDescription("Numeric amount spent")
        BigDecimal amount,

        @JsonPropertyDescription("3-letter currency code, e.g. UAH, USD, EUR. Default to UAH if not specified")
        String currency,

        @JsonPropertyDescription("Category of expense: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, or OTHER")
        String category,

        @JsonPropertyDescription("Date of expense in YYYY-MM-DD format")
        LocalDate expenseDate
) {}
