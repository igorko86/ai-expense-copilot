package com.copilot.backend.expense;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// Перелік наших вузькопрофільних агентів (аналог TypeScript Union Type: 'SQL_ANALYST' | 'DOCUMENT_EXPERT' | 'GENERAL_ADVISOR')
enum AgentType {
    SQL_ANALYST,
    DOCUMENT_EXPERT,
    GENERAL_ADVISOR
}

public record RouteDecisionDto(
        @JsonPropertyDescription("Choose SQL_ANALYST for questions about user's expenses, amounts, items, categories, or charts. Choose DOCUMENT_EXPERT for questions about uploaded PDF files or documents. Choose GENERAL_ADVISOR for greetings or general financial advice.")
        AgentType targetAgent,

        @JsonPropertyDescription("Short explanation in 5 words why this agent was chosen")
        String reason
) {}
