package com.aarash.rag.service.impl;

import com.aarash.rag.service.LLMService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Service
public class OpenAiLLMService implements LLMService {
    private final ChatClient chatClient;

    public OpenAiLLMService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateResponse(String userPrompt, String systemPrompt) {
        return chatClient.prompt()
                .user(userPrompt)
                .system(Optional.ofNullable(systemPrompt).orElse("You are a helpful assistant."))
                .options(ChatOptions.builder().temperature(.3).build())
                .call()
                .content();
    }

    public Flux<String> generateResponseStream(String userPrompt, String systemPrompt) {
        return chatClient.prompt()
                .user(userPrompt)
                .system(Optional.ofNullable(systemPrompt).orElse("You are a helpful assistant."))
                .options(ChatOptions.builder().temperature(.3).build())
                .stream()
                .content();
    }
}
