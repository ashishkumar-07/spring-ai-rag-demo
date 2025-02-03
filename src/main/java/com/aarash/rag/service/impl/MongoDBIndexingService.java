package com.aarash.rag.service.impl;

import com.aarash.rag.dto.IndexingResponse;
import com.aarash.rag.dto.IndexingStatus;
import com.aarash.rag.model.ProcessedDocumentDetail;
import com.aarash.rag.repository.ProcessedDocumentRepo;
import com.aarash.rag.service.IndexingService;
import lombok.SneakyThrows;
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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class MongoDBIndexingService implements IndexingService {

    private final TextSplitter textSplitter ;
    private final MongoDBAtlasVectorStore vectorStore;
    private final ProcessedDocumentRepo  processedDocumentRepo;
    private final MongoOperations mongoOperations;


    private static final String CUSTOM_METADATA_KEY = "labels";

    public MongoDBIndexingService(MongoDBAtlasVectorStore vectorStore, ProcessedDocumentRepo processedDocumentRepo, MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
        this.textSplitter = new TokenTextSplitter();
        this.vectorStore = vectorStore;
        this.processedDocumentRepo = processedDocumentRepo;
    }

    public IndexingResponse indexDocumentFromFilesystem(String sourcePath, List<String> keywords) {
        var resource = new FileSystemResource(sourcePath);

        return indexDocument(resource, keywords);
    }

    public IndexingResponse indexDocumentFromURL(String sourcePath, List<String> keywords) {
        UrlResource resource = null;
        try {
            resource = new UrlResource(sourcePath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + sourcePath, e);
        }
        return indexDocument(resource, keywords);
    }

    @SneakyThrows
    private IndexingResponse indexDocument(Resource resource, List<String> labels) {
        Assert.notNull(resource, "Resource must not be null!");
        Assert.isTrue( resource.exists(), "Resource must exist!");

        var docToProcess = processedDocumentRepo.findBySourcePath(resource.getURL().toString())
                .orElse(new ProcessedDocumentDetail(null,
                        resource.getURL().toString(),
                        StringUtils.EMPTY,
                        LocalDateTime.now(),
                        LocalDateTime.now()));

        String inputDocHash = calculateHash(resource);
        if(!(resource instanceof UrlResource) && docToProcess.getHash().equals(inputDocHash)){
            log.info("Document already processed: {}", resource.getURL().toString());
            return new IndexingResponse(IndexingStatus.ALREADY_PROCESSED, null);
        }
        log.info("Processing document: {}", resource.getDescription());

        docToProcess.setHash(inputDocHash);
        docToProcess.setLastProcessedAt(LocalDateTime.now());
        if(docToProcess.getProcessedDocumentId()!=null){
            log.info("Deleting existing embedding for document: {}", docToProcess.getProcessedDocumentId());
            deleteOldEmbeddings(docToProcess.getProcessedDocumentId());
        }

        var savedDoc = processedDocumentRepo.save(docToProcess);

        List<Document> splittedDocs = addDocumentToVectorStore(resource, savedDoc.getProcessedDocumentId(), labels);
        return new IndexingResponse(IndexingStatus.SUCCESS, null);
    }

    private void deleteOldEmbeddings(String processedDocumentId) {
        Criteria criteria = Criteria.where("metadata.processedDocumentId").in(processedDocumentId);
        mongoOperations.remove(Query.query(criteria), "vector_store");
    }

    private String calculateHash(Resource resource) {
        var lastModified = 0L;
        try {
            lastModified = resource.lastModified();
        } catch (Exception e) {
            log.warn("Failed to get last modified date for resource: {}", resource, e);
            lastModified = System.currentTimeMillis();
        }
        var original = resource.getDescription().toLowerCase() + "//" + lastModified;
        return DigestUtils.sha256Hex(original);
    }

    private List<Document> addDocumentToVectorStore(Resource resource, String processedDocumentId, List<String> labels) {

        var reader = new TikaDocumentReader(resource);
        List<Document> docs = reader.read();
        docs.forEach(doc -> addCustomMetaData(doc, labels));

        docs.forEach(doc -> doc.getMetadata().put("processedDocumentId", processedDocumentId));
        var splittedDocs =  textSplitter.apply(docs);

        vectorStore.add(splittedDocs);
        return splittedDocs;
    }

    private void addCustomMetaData(Document document, List<String> labels) {
        if(labels == null && labels.isEmpty()) {
            return ;
        }
        Assert.notNull(document, "Document must not be null!");
        document.getMetadata().putAll(Map.of(CUSTOM_METADATA_KEY, labels));
    }

}
