package com.aarash.rag.service;

import reactor.core.publisher.Flux;

public interface LLMService {

    public String generateResponse(String userPrompt, String systemPrompt);
    public Flux<String> generateResponseStream(String userPrompt, String systemPrompt);
}
