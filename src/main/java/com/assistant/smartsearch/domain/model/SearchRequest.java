package com.assistant.smartsearch.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class SearchRequest {
    // Basic search
    private String query;
    private String tableName;  // Optional - if not provided, will search across all tables
    private List<String> searchFields;

    // Advanced search options
    private boolean useFullTextSearch = true; // Enable/disable full-text search
    private String language = "french"; // Default to French, supported: french, arabic
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("french", "arabic");
    private Map<String, String> filters; // Field-value pairs for exact matching
    private int page = 0; // Pagination - page number (0-based)
    private int size = 20; // Page size
    private String sortField; // Field to sort by
    private boolean sortAscending = true; // Sort direction

    public void setLanguage(String language) {
        if (language != null && SUPPORTED_LANGUAGES.contains(language.toLowerCase())) {
            this.language = language.toLowerCase();
        } else {
            throw new IllegalArgumentException("Unsupported language. Supported languages are: " + 
                String.join(", ", SUPPORTED_LANGUAGES));
        }
    }

    // Additional search parameters
    private boolean highlightMatches = true; // Whether to include highlighting in results
    private List<String> returnFields; // Specific fields to return (null for all)

    // For future use with vector/semantic search
    private boolean useSemanticSearch = false;
    private float[] queryVector; // For vector search
}
