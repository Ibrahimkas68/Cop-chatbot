package com.assistant.smartsearch.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified search result containing only the essential fields for API response.
 * This DTO provides a clean, minimal response format with only the required fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedSearchResult {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("url")
    private String url;
    @JsonProperty("tag")
    private String tag;
    @JsonProperty("imageName")
    private String imageName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("score")
    private Double score;
    
    /**
     * Creates a SimplifiedSearchResult from a full SearchResult.
     * 
     * @param searchResult The full search result to convert
     * @return A simplified search result with only the essential fields
     */
    public static SimplifiedSearchResult from(SearchResult searchResult) {
        return new SimplifiedSearchResult(
            searchResult.getTitle(),
            searchResult.getUrl(),
            searchResult.getTag().toUpperCase(),
            searchResult.getImageName(),
            searchResult.getDescription(),
            searchResult.getScore()
        );
    }
}