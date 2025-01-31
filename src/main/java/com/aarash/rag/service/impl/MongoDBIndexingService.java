package com.aarash.rag.service.impl;

import com.aarash.rag.entity.ProcessedDocumentChunkDetail;
import com.aarash.rag.entity.ProcessedDocumentDetail;
import com.aarash.rag.repository.ProcessedDocumentChunkRepo;
import com.aarash.rag.repository.ProcessedDocumentRepo;
import com.aarash.rag.service.IndexingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class MongoDBIndexingService implements IndexingService {

    private final TextSplitter textSplitter ;
    private final MongoDBAtlasVectorStore vectorStore;
    private final ProcessedDocumentRepo  processedDocumentRepo;
    private final ProcessedDocumentChunkRepo  processedDocumentChunkRepo;


    private static final String CUSTOM_METADATA_KEY = "labels";

    public MongoDBIndexingService(MongoDBAtlasVectorStore vectorStore, ProcessedDocumentRepo processedDocumentRepo, ProcessedDocumentChunkRepo processedDocumentChunkRepo) {
        this.textSplitter = new TokenTextSplitter();
        this.vectorStore = vectorStore;
        this.processedDocumentRepo = processedDocumentRepo;
        this.processedDocumentChunkRepo = processedDocumentChunkRepo;
    }


    private void addCustomMetaData(Document document, List<String> labels) {
        if(labels == null && labels.isEmpty()) {
            return ;
        }
        Assert.notNull(document, "Document must not be null!");
        document.getMetadata().putAll(Map.of(CUSTOM_METADATA_KEY, labels));
    }

    public List<Document> indexDocumentFromFilesystem(String sourcePath, List<String> keywords) {
        var resource = new FileSystemResource(sourcePath);

        return indexDocument(resource, keywords);
    }

    public List<Document> indexDocumentFromURL(String sourcePath, List<String> keywords) {
        try {
            var resource = new UrlResource(sourcePath);

            return indexDocument(resource, keywords);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + sourcePath, e);
        }
    }

    private List<Document> processDocument(Resource resource, List<String> labels) {
        Assert.notNull(resource, "Resource must not be null!");
        Assert.isTrue( resource.exists(), "Resource must exist!");

        var reader = new TikaDocumentReader(resource);
        List<Document> docs = reader.read();
        docs.forEach(doc -> addCustomMetaData(doc, labels));
        var splittedDocs =  textSplitter.apply(docs);
        vectorStore.add(splittedDocs);
        return splittedDocs;
    }

    private String calculateHash(Resource resource) {
        var lastModified = 0L;
        try {
            lastModified = resource.lastModified();
        } catch (Exception e) {
            log.warn("Failed to get last modified date for resource: {}", resource, e);
        }
        var original = resource.getDescription().toLowerCase() + "//" + lastModified;
        return DigestUtils.sha256Hex(original);
    }

    private List<Document> indexDocument(Resource resource, List<String> labels) {

        ProcessedDocumentDetail docToProcess = processedDocumentRepo.findBySourcePath(resource.getDescription())
                .orElse(new ProcessedDocumentDetail(null, resource.getDescription(), StringUtils.EMPTY, LocalDateTime.now(), LocalDateTime.now()));

        String inputDocHash = calculateHash(resource);

        if(!(resource instanceof UrlResource) && docToProcess.getHash().equals(inputDocHash)){
            log.info("Document already processed: {}", resource.getDescription());
            return List.of();
        }
        log.info("Processing document: {}", resource.getDescription());

        List<Document> splittedDocs = processDocument(resource, labels);
        docToProcess.setHash(inputDocHash);
        docToProcess.setLastProcessedAt(LocalDateTime.now());
        if(docToProcess.getProcessedDocumentId()!=null){
            log.info("Deleting existing chunks for document: {}", docToProcess.getProcessedDocumentId());
            deleteExistingChunks(docToProcess.getProcessedDocumentId());
        }

        ProcessedDocumentDetail savedDoc = processedDocumentRepo.save(docToProcess);

        splittedDocs.forEach(chunk -> {
            var chunkEntity = new ProcessedDocumentChunkDetail(chunk.getId(), savedDoc.getProcessedDocumentId());
            processedDocumentChunkRepo.save(chunkEntity);
        });

        return splittedDocs;

    }

    private void deleteExistingChunks(String processedDocumentId) {
        List<String> chunkIds = processedDocumentChunkRepo.findIdByProcessedDocumentId(processedDocumentId);
        vectorStore.delete(chunkIds);
        processedDocumentRepo.deleteAllById(chunkIds);
    }
}
