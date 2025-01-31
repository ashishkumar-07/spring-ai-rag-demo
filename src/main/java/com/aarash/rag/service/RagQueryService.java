package com.aarash.rag.service;

import reactor.core.publisher.Flux;

public interface RagQueryService {
    String generateResponse(String userPrompt, String systemPrompt, int topK);

    Flux<String> generateResponseStream(String userPrompt, String systemPrompt, int topK);
}
