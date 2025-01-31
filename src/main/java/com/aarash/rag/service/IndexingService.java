package com.aarash.rag.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface IndexingService {
    public List<Document> indexDocumentFromFilesystem(String sourcePath, List<String> keywords);
    public List<Document> indexDocumentFromURL(String sourcePath, List<String> keywords);
}
