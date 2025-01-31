package com.aarash.rag.repository;

import com.aarash.rag.entity.ProcessedDocumentChunkDetail;
import com.aarash.rag.entity.ProcessedDocumentDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.Flow;

@Repository
public interface ProcessedDocumentChunkRepo extends MongoRepository<ProcessedDocumentChunkDetail, String>{
    List<ProcessedDocumentChunkDetail> findByProcessedDocumentId(String processedDocumentId);
    List<String> findIdByProcessedDocumentId(String processedDocumentId);

    void deleteByProcessedDocumentId(String processedDocumentId);
}

