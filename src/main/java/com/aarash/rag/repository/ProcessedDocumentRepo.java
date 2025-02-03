package com.aarash.rag.repository;

import com.aarash.rag.model.ProcessedDocumentDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedDocumentRepo extends MongoRepository<ProcessedDocumentDetail, String>{
    Optional<ProcessedDocumentDetail> findBySourcePath(String sourcePath);
}
