package com.aarash.rag.controller;

import com.aarash.rag.dto.FileSystemIndexingRequest;
import com.aarash.rag.dto.QuestionRequest;
import com.aarash.rag.dto.URLIndexingRequest;
import com.aarash.rag.service.RagQueryService;
import com.aarash.rag.service.impl.MongoDBIndexingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("api/v1/rag")
public class RagController {

    private final MongoDBIndexingService indexingService;
    private final RagQueryService ragQueryService;

    public RagController(MongoDBIndexingService indexingService, RagQueryService ragQueryService) {
        this.indexingService = indexingService;
        this.ragQueryService = ragQueryService;
    }


    @PostMapping(path = "/indexing/filesystem")
    public List<Document> indexDocumentFromFilesystem(
            @RequestBody @Valid FileSystemIndexingRequest request) {
        var indexedDocuments = indexingService.indexDocumentFromFilesystem(
                request.path(),
                request.labels());

        return indexedDocuments;
    }

    @PostMapping(path = "/indexing/url")
    public List<Document> indexDocumentFromURL(
            @RequestBody @Valid URLIndexingRequest request) {
        var indexedDocuments = indexingService.indexDocumentFromURL(
                request.url(),
                request.labels());

        return indexedDocuments;
    }

    @PostMapping(path = "/ask")
    public String queryAIUsingRag(@RequestBody @Valid QuestionRequest request,
                                  @RequestParam(name = "top-k", required = false, defaultValue = "0") int topK) {
        return ragQueryService.generateResponse(request.userPrompt(),request.systemPrompt(), topK);
    }

    @PostMapping(path = "/ask/streaming")
    public Flux<String> queryAIUsingRagSteaming(@RequestBody @Valid QuestionRequest request,
                                                @RequestParam(name = "top-k", required = false, defaultValue = "0") int topK) {
        return ragQueryService.generateResponseStream(request.userPrompt(), request.systemPrompt(), topK);

    }
}
