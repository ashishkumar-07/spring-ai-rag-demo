package com.aarash.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.autoconfigure.vectorstore.mongo.MongoDBAtlasVectorStoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;

@SpringBootApplication
@Slf4j
public class SpringAiRagDemoApplication {

	@Autowired
	MongoDBAtlasVectorStoreProperties mongoDBAtlasVectorStoreProperties;

	@Autowired
	MongoOperations mongoOperations;

	public static void main(String[] args) {
		SpringApplication.run(SpringAiRagDemoApplication.class, args);
	}

	@EventListener
	public void createIndex(ContextRefreshedEvent event) {
		log.info("Creating index");
		mongoOperations.indexOps(mongoDBAtlasVectorStoreProperties.getCollectionName())
				.ensureIndex(new Index().on("metadata.processedDocumentId", Sort.Direction.ASC));
	}
}
