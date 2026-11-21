package com.copilot.backend.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTools expenseTools;
    private final VectorStore vectorStore;

    public Expense parseAndSave(String rawText) {
        String today = LocalDate.now().toString();

        ParsedExpenseDto parsed = chatClient.prompt()
                .system("""
                        You are a financial data extraction assistant.
                        Extract expense details from the user's text.
                        Today's date is %s. If the date is not mentioned or says 'today', use today's date.
                        If currency is missing, assume UAH.
                        Classify category strictly into: FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, or OTHER.
                        """.formatted(today))
                .user(rawText)
                .call()
                .entity(ParsedExpenseDto.class);

        Expense expense = new Expense(
                parsed.description(),
                parsed.amount(),
                parsed.currency() != null ? parsed.currency().toUpperCase() : "UAH",
                parsed.category() != null ? parsed.category().toUpperCase() : "OTHER",
                parsed.expenseDate() != null ? parsed.expenseDate() : LocalDate.now()
        );

        return expenseRepository.save(expense);
    }

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

    public Flux<String> askAssistantStream(String question) {
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build()
        );

        String documentContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        return chatClient.prompt()
                .system("""
                        You are a helpful personal finance assistant.
                        Use the provided tools to fetch real expense data from the database.
                        Use the retrieved document context below to answer questions about uploaded PDFs or rules.
                        Do not guess or make up numbers.
                        Answer in the same language as the user's question (e.g., Ukrainian or English).
                        
                        DOCUMENT CONTEXT:
                        %s
                        """.formatted(documentContext.isEmpty() ? "No documents uploaded yet." : documentContext))
                .user(question)
                .tools(expenseTools)
                .stream() // <-- Замість .call() викликаємо .stream()
                .content();
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
}
