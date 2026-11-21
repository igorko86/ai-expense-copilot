package com.copilot.backend.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ai/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;


    public record ParseRequest(String text) {}
    public record ChatRequest(String question) {}
    public record ChatResponse(String answer) {}

    @PostMapping("/parse")
    public Expense parseExpense(@RequestBody ParseRequest request) {
        return expenseService.parseAndSave(request.text());
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = expenseService.askAssistant(request.question());
        return new ChatResponse(answer);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String question) {
        return expenseService.askAssistantStream(question);
    }

    @GetMapping
    public List<Expense> getAll() {
        return expenseService.getAllExpenses();
    }
}
