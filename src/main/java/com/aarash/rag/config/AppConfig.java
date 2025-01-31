package com.aarash.rag.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.vectorstore.mongo.MongoDBAtlasVectorStoreProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class AppConfig {
    @Bean
    ChatClient openAiChatClient(ChatClient.Builder  builder) {
        return builder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }
}
