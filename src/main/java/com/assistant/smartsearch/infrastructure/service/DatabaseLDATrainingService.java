package com.assistant.smartsearch.infrastructure.service;

import com.assistant.smartsearch.domain.port.SearchUseCase;
import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to train LDA model with content from the database.
 * Fetches documents and triggers model training.
 */
@Slf4j
@Service
public class DatabaseLDATrainingService {
    
    private final LDATopicModelService ldaService;
    private final SearchUseCase searchUseCase;
    
    @Autowired
    public DatabaseLDATrainingService(
            LDATopicModelService ldaService,
            SearchUseCase searchUseCase) {
        this.ldaService = ldaService;
        this.searchUseCase = searchUseCase;
    }
    
    /**
     * Train LDA model with documents from the database
     * @param tableName Table to fetch documents from
     */
    public void trainModelFromDatabase(String tableName) {
        log.info("Starting LDA model training from database table: {}", tableName);
        
        try {
            // Fetch all documents from the specified table
            Map<String, String> documents = fetchDocumentsFromDatabase(tableName);
            
            if (documents.isEmpty()) {
                log.warn("No documents found in table: {}", tableName);
                return;
            }
            
            log.info("Fetched {} documents from table: {}", documents.size(), tableName);
            
            // Train the model asynchronously
            trainModelAsync(documents);
            
        } catch (Exception e) {
            log.error("Error training LDA model from database", e);
        }
    }
    
    /**
     * Train model asynchronously (non-blocking)
     */
    @Async
    public void trainModelAsync(Map<String, String> documents) {
        ldaService.trainModel(documents);
    }
    
    /**
     * Fetch all documents from database
     * Combines title and content for better topic modeling
     */
    private Map<String, String> fetchDocumentsFromDatabase(String tableName) {
        Map<String, String> documents = new HashMap<>();
        
        try {
            // Create an empty search to get all documents
            SearchRequest request = new SearchRequest();
            request.setQuery("*");
            request.setTableName(tableName);
            request.setSize(10000); // Get all documents
            
            // Execute search to get all documents
            List<SearchResult> results = searchUseCase.execute(request);
            
            for (SearchResult result : results) {
                if (result.getId() != null) {
                    // Combine title and description for richer content
                    StringBuilder content = new StringBuilder();
                    
                    if (result.getTitle() != null && !result.getTitle().isEmpty()) {
                        content.append(result.getTitle()).append(" ");
                    }
                    
                    if (result.getDescription() != null && !result.getDescription().isEmpty()) {
                        content.append(result.getDescription()).append(" ");
                    }
                    
                    // Get highlighted content if available
                    String highlighted = result.getHighlightedContent();
                    if (highlighted != null && !highlighted.isEmpty()) {
                        content.append(highlighted);
                    }
                    
                    if (content.length() > 0) {
                        documents.put(result.getId(), content.toString());
                    }
                }
            }
            
            log.info("Successfully fetched {} documents from database", documents.size());
            
        } catch (Exception e) {
            log.error("Error fetching documents from database", e);
        }
        
        return documents;
    }
    
    /**
     * Get current model status
     */
    public Map<String, Object> getModelStatus() {
        return ldaService.getModelInfo();
    }
    
    /**
     * Check if model is trained
     */
    public boolean isModelTrained() {
        return ldaService.isModelTrained();
    }
    
    /**
     * Get topics discovered by the model
     */
    public Map<Integer, List<String>> getDiscoveredTopics() {
        return ldaService.getTopicsTerms();
    }
    
    /**
     * Clear the model
     */
    public void clearModel() {
        ldaService.clearModel();
    }
}
