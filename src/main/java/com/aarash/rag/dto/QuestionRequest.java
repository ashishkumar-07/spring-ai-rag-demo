package com.aarash.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequest(
    String systemPrompt,
    @NotBlank String userPrompt
) {

}
