package com.aarash.rag.service;

import com.aarash.rag.dto.IndexingResponse;

import java.util.List;

public interface IndexingService {
    public IndexingResponse indexDocumentFromFilesystem(String sourcePath, List<String> keywords);
    public IndexingResponse indexDocumentFromURL(String sourcePath, List<String> keywords);
}
