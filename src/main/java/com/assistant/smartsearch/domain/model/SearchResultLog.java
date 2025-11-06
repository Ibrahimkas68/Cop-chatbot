package com.assistant.smartsearch.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultLog {
    private Long documentId;
    private String title;
    private Double score;
    private List<String> matchedKeywords;
    private int position;
}
