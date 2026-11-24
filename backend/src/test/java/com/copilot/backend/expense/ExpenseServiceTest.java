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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
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
        // 1. Arrange (Готуємо тестові дані для вкладеного DTO зі списком товарів)
        ExpenseItemDto item = new ExpenseItemDto(
                "Silpo",
                new BigDecimal("450.00"),
                "UAH",
                "FOOD",
                LocalDate.of(2026, 11, 21)
        );
        ParsedExpenseDto mockDto = new ParsedExpenseDto(List.of(item));

        // Готуємо позитивну відповідь від Агента-Критика, щоб цикл завершився на 1-й спробі
        AuditResultDto mockAudit = new AuditResultDto(true, "OK");

        // Мокаємо виклик Агента-Виконавця
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(ParsedExpenseDto.class))
                .thenReturn(mockDto);

        // Мокаємо виклик Агента-Критика
        when(chatClient.prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .entity(AuditResultDto.class))
                .thenReturn(mockAudit);

        // Мокаємо масове збереження saveAll (повертаємо той самий список, який передали)
        when(expenseRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act (Викликаємо наш метод)
        List<Expense> results = expenseService.parseAndSave("Сільпо 450 грн");

        // 3. Assert (Перевіряємо результат)
        assertNotNull(results);
        assertEquals(1, results.size()); // Перевіряємо, що зберігся 1 елемент

        Expense savedExpense = results.get(0);
        assertEquals("Silpo", savedExpense.getDescription());
        assertEquals(new BigDecimal("450.00"), savedExpense.getAmount());
        assertEquals("FOOD", savedExpense.getCategory());

        // Перевіряємо, що викликався саме saveAll
        verify(expenseRepository).saveAll(anyList());
    }
}
