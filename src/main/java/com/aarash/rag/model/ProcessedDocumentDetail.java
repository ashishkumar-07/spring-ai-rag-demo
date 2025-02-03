package com.aarash.rag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "processed_documents")
@AllArgsConstructor
@Data
public class ProcessedDocumentDetail {

    @Id
    @Field(name = "_id")
    private String  processedDocumentId;
    private String sourcePath;

    private String hash;

    private LocalDateTime firstProcessedAt;

    private LocalDateTime lastProcessedAt;
}
