package com.copilot.backend.expense;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest // Каже Spring: "Підніми справжній контекст додатку з налаштуваннями Ollama з application.yml"
@Tag("ai-eval")
class ExpenseAiEvalTest {

    // Spring сам підставить готовий білдер, який вже налаштований на вашу модель llama3.2!
    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void evaluateFinancialAssistantAccuracy() {
        ChatClient judgeClient = chatClientBuilder.build();

        // 1. Arrange: Еталонні дані (Ground Truth) та реальна відповідь нашого асистента
        String userQuestion = "Скільки грошей я витратив на каву цього місяця?";
        String groundTruthFact = "Total spent on coffee is 120.00 UAH (1 purchase in ATB).";
        String actualAssistantResponse = "За даними вашої бази витрат, на каву ви витратили рівно 120 грн.";

        // 2. Act: Просимо нейромережу-Суддю оцінити відповідь асистента
        EvalVerdictDto verdict = judgeClient.prompt()
                .system("""
                        You are an impartial AI Quality Assurance Judge.
                        Compare the ACTUAL RESPONSE against the GROUND TRUTH FACT for the given USER QUESTION.
                        Rules:
                        - Set 'passed' to true ONLY if all numbers and facts in ACTUAL RESPONSE match the GROUND TRUTH FACT.
                        - Ignore differences in wording or language (Ukrainian vs English is fine).
                        - Treat "UAH", "грн", and "гривень" as the exact same currency (Ukrainian Hryvnia).
                        - If numbers differ or facts are hallucinated, set 'passed' to false.
                        """)
                .user("""
                        USER QUESTION: %s
                        GROUND TRUTH FACT: %s
                        ACTUAL RESPONSE: %s
                        """.formatted(userQuestion, groundTruthFact, actualAssistantResponse))
                .call()
                .entity(EvalVerdictDto.class);

        System.out.println("⚖️ Вердикт Судді: passed=" + verdict.passed() + ", score=" + verdict.score() + "/5");
        System.out.println("📝 Пояснення Судді: " + verdict.reasoning());

        // 3. Assert: Перевіряємо, що Суддя схвалив відповідь
        assertNotNull(verdict);
        assertTrue(Boolean.TRUE.equals(verdict.passed()), "AI Judge failed the response: " + verdict.reasoning());
        assertTrue(verdict.score() != null && verdict.score() >= 4, "Score too low: " + verdict.score());
    }
}
