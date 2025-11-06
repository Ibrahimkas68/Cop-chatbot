package com.assistant.smartsearch.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "search_logs")
public class SearchLog {
    @Id
    private String id;
    private String originalQuery;
    private String queryLanguage;
    private List<String> extractedKeywords;
    private List<String> normalizedKeywords;
    private List<SearchResultLog> results;
    private UserInteraction userInteraction;
    private SearchContext searchContext;
    private Filters filters;
    private Performance performance;
    private Date timestamp;
    private String date;
    private int hour;
}
