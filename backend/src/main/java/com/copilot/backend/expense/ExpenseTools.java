package com.copilot.backend.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseTools {

    private final ExpenseRepository expenseRepository;

    @Tool(description = "Get a list of all user expenses from the database")
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Tool(description = "Get expenses filtered by category: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, or OTHER")
    public List<Expense> getExpensesByCategory(
            @ToolParam(description = "Expense category in uppercase, e.g. FOOD, TRANSPORT") String category) {
        return expenseRepository.findByCategoryIgnoreCase(category);
    }

    @Tool(description = "Calculate the total amount spent across all expenses")
    public BigDecimal getTotalSpending() {
        return expenseRepository.findAll().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = "Calculate the exact total amount spent for a specific category (e.g. FOOD, TRANSPORT, ENTERTAINMENT)")
    public BigDecimal getTotalSpendingByCategory(
            @ToolParam(description = "Expense category in uppercase, e.g. FOOD, TRANSPORT") String category) {
        return expenseRepository.findByCategoryIgnoreCase(category).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Tool(description = "Calculate the exact total amount spent on a specific item or keyword in description (e.g. 'coffee', 'кава', 'milk', 'taxi', 'bread')")
    public BigDecimal getTotalSpendingByItemName(
            @ToolParam(description = "Keyword or item name to search in expense descriptions, e.g. 'coffee' or 'кава'") String itemName) {
        return expenseRepository.findByDescriptionContainingIgnoreCase(itemName).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
