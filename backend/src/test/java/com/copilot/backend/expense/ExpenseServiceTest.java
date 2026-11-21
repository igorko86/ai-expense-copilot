package com.copilot.backend.expense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseTools expenseTools;

    @Mock
    private VectorStore vectorStore;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(chatClient, expenseRepository, expenseTools, vectorStore);
    }

    @Test
    void shouldParseAndSaveExpenseCorrectly() {
        // 1. Arrange (Готуємо тестові дані)
        ParsedExpenseDto mockDto = new ParsedExpenseDto(
                "Silpo",
                new BigDecimal("450.00"),
                "UAH",
                "FOOD",
                LocalDate.of(2026, 11, 21)
        );

        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(ParsedExpenseDto.class))
                .thenReturn(mockDto);

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act (Викликаємо наш метод)
        Expense result = expenseService.parseAndSave("Сільпо 450 грн");

        // 3. Assert (Перевіряємо результат)
        assertNotNull(result);
        assertEquals("Silpo", result.getDescription());
        assertEquals(new BigDecimal("450.00"), result.getAmount());
        assertEquals("FOOD", result.getCategory());
        verify(expenseRepository).save(any(Expense.class));
    }
}
