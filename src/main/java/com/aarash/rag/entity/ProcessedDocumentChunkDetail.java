package com.aarash.rag.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processed_document_chunks")
@AllArgsConstructor
@Data
public class ProcessedDocumentChunkDetail {

    @Id
    private String id;
    private String processedDocumentId;
}
