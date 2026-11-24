package com.copilot.backend.expense;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.messages.Message;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTools expenseTools;
    private final VectorStore vectorStore;

    // Зберігаємо історію повідомлень для кожного чату (chatId -> список повідомлень)
    private final Map<String, List<Message>> chatHistory = new ConcurrentHashMap<>();

    public List<Expense> parseAndSave(String rawText) {
        String today = LocalDate.now().toString();
        ParsedExpenseDto currentParsed = null;
        String criticFeedback = "";

        // Запускаємо цикл самоперевірки (максимум 3 спроби, щоб уникнути нескінченного циклу!)
        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("🔄 Спроба парсингу №" + attempt);
            boolean hasFeedback = criticFeedback != null && !criticFeedback.isBlank();
            // 1. АГЕНТ-ВИКОНАВЕЦЬ: витягує або виправляє дані з урахуванням критики
            String promptForWorker = !hasFeedback
                    ? rawText
                    : "Original text: '%s'. Your previous extraction was: %s. Fix it based on this feedback: %s"
                    .formatted(rawText, currentParsed, criticFeedback);

            currentParsed = chatClient.prompt()
                    .system("""
                            You are a financial data extraction assistant.
                            Extract the store/description and a list of ALL individual item prices into 'itemAmounts'.
                            For example, if text says 'bread 30, milk 45, coffee 120', itemAmounts MUST be.
                            Today's date is %s. If currency is missing, assume UAH.
                            Classify category strictly into: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, or OTHER.
                            """.formatted(today))
                    .user(promptForWorker)
                    .call()
                    .entity(ParsedExpenseDto.class);

            // 2. АГЕНТ-КРИТИК: перевіряє роботу першого агента
            AuditResultDto audit = chatClient.prompt()
                    .system("""
                            You are a strict financial auditor. Compare the original user text with the extracted JSON data.
                            Check if:
                            1) The total amount is mathematically correct (sum of all items in the text).
                            2) The category is logical (e.g., coffee/groceries = FOOD, taxi = TRANSPORT).
                            3) The currency is correct.
                            """)
                    .user("Original text: '%s'\nExtracted data: %s".formatted(rawText, currentParsed))
                    .call()
                    .entity(AuditResultDto.class);

            // Якщо Критик схвалив результат — виходимо з циклу (break)!
            if (audit != null &&  Boolean.TRUE.equals(audit.approved())) {
                log.info("✅ Аудит пройдено успішно на спробі №" + attempt);
                break;
            }

            // Якщо є зауваження — записуємо їх для наступної ітерації циклу
            criticFeedback = audit != null ? audit.feedback() : "Check math and category again.";
            log.info("⚠️ Критик знайшов помилку: " + criticFeedback);
        }

            log.info("⚠️ currentParsed: " + currentParsed);
        // Фільтруємо можливі "порожні" галюцинації моделі без ціни або назви перед записом у БД
        List<Expense> expensesToSave = currentParsed.items().stream()
                .filter(item -> item.description() != null && !item.description().isBlank())
                .filter(item -> item.amount() != null)
                .map(item -> new Expense(
                        item.description(),
                        item.amount(),
                        item.currency() != null ? item.currency().toUpperCase() : "UAH",
                        item.category() != null ? item.category().toUpperCase() : "OTHER",
                        item.expenseDate() != null ? item.expenseDate() : LocalDate.now()
                ))
                .toList();

        log.info("✅ Зберігаємо в базу окремих позицій: " + expensesToSave.size());
        return expenseRepository.saveAll(expensesToSave);    }

    public String askAssistant(String question) {
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest
                        .builder()
                        .query(question)
                        .topK(4)
                        .build()
        );
        List<String> contentList = similarDocs.stream().map(Document::getText).toList();
        return chatClient.prompt()
                .system("""
                        You are a helpful personal finance assistant.
                        Use the provided tools to fetch real expense data from the database.
                        Use the retrieved document context below to answer questions about uploaded PDFs or rules.
                        Do not guess or make up numbers.
                        Answer in the same language as the user's question (e.g., Ukrainian or English).

                        DOCUMENT CONTEXT:
                        %s
                        """.formatted(contentList.isEmpty() ? "No documents uploaded yet." : String.join("\n", contentList)))
                .user(question)
                .tools(expenseTools)
                .call()
                .content();
    }

    public Flux<String> askAssistantStream(String chatId, String question) {
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build()
        );

        String documentContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 2. Дістаємо історію поточного чату (беремо останні 10 повідомлень, щоб не переповнити контекст)
        List<Message> history = chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>());
        List<Message> recentHistory = history.size() > 10
                ? new ArrayList<>(history.subList(history.size() - 10, history.size()))
                : new ArrayList<>(history);
        // Додаємо нове питання користувача в історію
        history.add(new UserMessage(question));

        // Буфер для збирання повної відповіді ШІ зі стріму, щоб зберегти її в історію
        StringBuilder fullResponseBuffer = new StringBuilder();
        // 1. Перевіряємо звичайним Java-кодом, чи просить користувач графік
        String lowerQuestion = question.toLowerCase();
        boolean isChartRequested = lowerQuestion.contains("графік")
                || lowerQuestion.contains("діаграм")
                || lowerQuestion.contains("chart")
                || lowerQuestion.contains("graph");

        // 2. Інструкцію про тег [RENDER_CHART] додаємо в промпт ТІЛЬКИ якщо користувач реально попросив графік
        String chartInstruction = isChartRequested
                ? "The user asked for a visual chart. You MUST append the exact token [RENDER_CHART] at the very end of your response."
                : "Do NOT output [RENDER_CHART] under any circumstances.";

        return chatClient.prompt()
                .system("""
                        You are a helpful personal finance assistant.
                        Use the provided tools to fetch real expense data from the database.
                        Use the retrieved document context below to answer questions about uploaded PDFs or rules.
                        Do not guess or make up numbers.
                        Answer in the same language as the user's question (e.g., Ukrainian or English).
                        
                        DOCUMENT CONTEXT:
                        %s
                        """.formatted(chartInstruction, documentContext.isEmpty() ? "No documents uploaded yet." : documentContext))
                .messages(recentHistory) // <-- Передаємо історію попередніх повідомлень!
                .user(question)
                .tools(expenseTools)
                .stream() // <-- Замість .call() викликаємо .stream()
                .content()
                .doOnNext(fullResponseBuffer::append) // Накопичуємо кожен токен;
                .doOnComplete(() -> {
                    // Коли відповідь повністю згенерована — зберігаємо її в історію чату
                    history.add(new AssistantMessage(fullResponseBuffer.toString()));
                });
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
}
