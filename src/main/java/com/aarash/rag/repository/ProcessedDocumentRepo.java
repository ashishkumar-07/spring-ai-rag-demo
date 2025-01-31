package com.aarash.rag.repository;

import com.aarash.rag.entity.ProcessedDocumentDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface ProcessedDocumentRepo extends MongoRepository<ProcessedDocumentDetail, String>{
    Optional<ProcessedDocumentDetail> findBySourcePath(String sourcePath);
}
