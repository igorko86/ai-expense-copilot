package com.copilot.backend.expense;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// 1. Окремий товар або послуга з тексту користувача
record ExpenseItemDto(
        @JsonPropertyDescription("Name of the specific item or service, e.g. 'Bread', 'Milk', 'Taxi Uklon'")
        String description,

        @JsonPropertyDescription("Exact price of this specific item")
        BigDecimal amount,

        @JsonPropertyDescription("3-letter currency code, e.g. UAH, USD, EUR")
        String currency,

        @JsonPropertyDescription("Strictly one of: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, OTHER")
        String category,

        @JsonPropertyDescription("Date of expense in YYYY-MM-DD format")
        LocalDate expenseDate
) {}

// 2. Список усіх товарів, які AI знайшов у повідомленні користувача
public record ParsedExpenseDto(
        @JsonPropertyDescription("List of ALL individual expenses/items extracted from the user's text")
        List<ExpenseItemDto> items
) {}