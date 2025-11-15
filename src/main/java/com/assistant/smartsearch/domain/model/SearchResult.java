package com.assistant.smartsearch.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single search result with metadata and highlighting.
 */
@Data
@Document(collection = "search_results")
public class SearchResult {
    /**
     * Unique identifier for this search result
     */
    @Id
    private String id;
    
    /**
     * The ID of the original record in the source table
     */
    private Long originalId;
    
    /**
     * The original search query that produced this result
     */
    private String query;
    
    /**
     * Name of the table this result came from
     */
    private String tableName;
    
    /**
     * The actual data fields from the database record
     */
    private Map<String, Object> data;
    
    /**
     * Relevance score (higher is more relevant)
     */
    private Double score;
    
    /**
     * List of keywords that matched this result
     */
    private List<String> matchedKeywords;
    
    /**
     * HTML-formatted text with search terms highlighted
     */
    private String highlightedText;
    
    /**
     * For pagination - the total number of results available
     */
    private Long totalResults;
    
    /**
     * For pagination - the current page number (0-based)
     */
    private Integer pageNumber;
    
    /**
     * For pagination - number of results per page
     */
    private Integer pageSize;
    
    /**
     * The title of the search result
     */
    private String title;

    /**
     * The URL of the result page.
     */
    private String url;

    /**
     * The name of the image associated with the result.
     */
    private String imageName;
    
    /**
     * A short description of the content
     */
    private String description;

    /**
     * A tag representing the type/category of the result (e.g., actualites, GUIDES.PARENTS, ADVICES.TEACHERS)
     */
    private String tag;
    
    /**
     * Additional metadata about the search result
     */
    private Map<String, Object> metadata;
    
    /**
     * Topic distribution for this document
     */
    private Map<String, Double> topicDistribution;
    
    /**
     * The dominant topic for this document
     */
    private String dominantTopic;
    
    /**
     * Similar documents based on topic modeling
     */
    private List<SearchResult> similarDocuments = new ArrayList<SearchResult>();
    
    /**
     * Gets the highlighted text if available, otherwise falls back to a field value
     */
    public String getHighlightedContent() {
        if (highlightedText != null && !highlightedText.isEmpty()) {
            return highlightedText;
        }
        
        // Fallback to the first text field if no highlighting is available
        if (data != null) {
            return data.values().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> s.length() > 0 && s.length() < 1000) // Reasonable length
                .findFirst()
                .orElse("");
        }
        
        return "";
    }
}
