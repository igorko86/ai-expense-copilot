package com.copilot.backend.expense;

import com.copilot.backend.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @Tool(description = "Get a list of all user expenses from the database")
    public List<Expense> getAllExpenses() {
        Long id = securityUtils.getCurrentUserId();
        return expenseRepository.findAllByUserId(id);
    }

    @Tool(description = "Get expenses filtered by category: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, or OTHER")
    public List<Expense> getExpensesByCategory(
            @ToolParam(description = "Expense category in uppercase, e.g. FOOD, TRANSPORT") String category) {
        Long id = securityUtils.getCurrentUserId();
        return expenseRepository.findByUserIdAndCategoryIgnoreCase(id, category);
    }

    @Tool(description = "Calculate the total amount spent across all expenses")
    public BigDecimal getTotalSpending() {
        return getAllExpenses().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = "Calculate the exact total amount spent for a specific category (e.g. FOOD, TRANSPORT, ENTERTAINMENT)")
    public BigDecimal getTotalSpendingByCategory(
            @ToolParam(description = "Expense category in uppercase, e.g. FOOD, TRANSPORT") String category) {
        Long id = securityUtils.getCurrentUserId();
        return expenseRepository.findByUserIdAndCategoryIgnoreCase(id, category).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Tool(description = "Calculate the exact total amount spent on a specific item or keyword in description (e.g. 'coffee', 'кава', 'milk', 'taxi', 'bread')")
    public BigDecimal getTotalSpendingByItemName(
            @ToolParam(description = "Keyword or item name to search in expense descriptions, e.g. 'coffee' or 'кава'") String itemName) {
        Long id = securityUtils.getCurrentUserId();
        return expenseRepository.findByUserIdAndDescriptionContainingIgnoreCase(id, itemName).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
