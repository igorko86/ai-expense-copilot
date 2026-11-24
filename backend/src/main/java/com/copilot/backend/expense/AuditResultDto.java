package com.copilot.backend.expense;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// Це структура відповіді для нашого другого агента — Критика (Аудитора)
public record AuditResultDto(
        @JsonPropertyDescription("True if the extracted data accurately matches the raw text math and category, false otherwise")
        Boolean approved,

        @JsonPropertyDescription("If approved is false, explain specifically what is wrong and how to fix it. If true, write 'OK'")
        String feedback
) {}