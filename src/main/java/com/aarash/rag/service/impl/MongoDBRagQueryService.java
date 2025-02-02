package com.aarash.rag.service.impl;

import com.aarash.rag.service.RagQueryService;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class MongoDBRagQueryService implements RagQueryService {
    private static final String KEY_CUSTOM_CONTEXT = "customContext";
    private static final String KEY_QUESTION = "question";
    private static final int TOP_K = 1;
    private static final double SIMILARITY_THRESHOLD = 0.7;

    private final OpenAiLLMService openAiService;
    private final VectorStore vectorStore;
    private final PromptTemplate basicAugmentationTemplate;


    public MongoDBRagQueryService(OpenAiLLMService openAiService, VectorStore vectorStore) {
        this.openAiService = openAiService;
        this.vectorStore = vectorStore;
        var ragBasicPromptTemplate = new ClassPathResource("prompts/rag-template.st");
        this.basicAugmentationTemplate = new PromptTemplate(ragBasicPromptTemplate);
    }

    @Override
    public String generateResponse(String userPrompt, String systemPrompt, int topK) {
        var customContext = retrieveCustomContext(userPrompt, topK);
        var augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);

        return openAiService.generateResponse(augmentedUserPrompt, systemPrompt);
    }

    @Override
    public Flux<String> generateResponseStream(String userPrompt, String systemPrompt, int topK) {
        var customContext = retrieveCustomContext(userPrompt, topK);
        var augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);
        return openAiService.generateResponseStream(augmentedUserPrompt, systemPrompt);
    }

    private String retrieveCustomContext(String userPrompt, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userPrompt)
                .topK(topK)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        String context = documents
                .stream()
                .map(document -> document.getContent())
                .reduce(new StringBuilder(), (sb, content) -> sb.append(content).append("\n"), (sb1, sb2) -> sb1.append(sb2).append("\n"))
                .toString();
        return context;
    }

    private String augmentUserPrompt(String userPrompt, String customContext) {
        return basicAugmentationTemplate.render(Map.of(KEY_CUSTOM_CONTEXT, customContext, KEY_QUESTION, userPrompt));
    }

}
