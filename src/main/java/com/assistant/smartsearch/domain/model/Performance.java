package com.assistant.smartsearch.domain.model;

import lombok.Data;

@Data
public class Performance {
    private long searchTimeMs;
    private int totalResults;
    private int resultsReturned;
}
