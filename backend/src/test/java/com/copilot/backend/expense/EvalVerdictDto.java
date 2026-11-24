package com.copilot.backend.expense;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// Структура оцінки, яку виставляє нейромережа-Суддя під час тестування
public record EvalVerdictDto(
        @JsonPropertyDescription("True if the assistant's answer factually matches the expected ground truth without hallucinations, false otherwise")
        Boolean passed,

        @JsonPropertyDescription("Score from 1 to 5, where 5 is completely accurate and helpful")
        Integer score,

        @JsonPropertyDescription("Brief explanation of why the answer passed or failed")
        String reasoning
) {}
