package com.assistant.smartsearch.domain.model;

import lombok.Data;

@Data
public class Filters {
    private String category;
    private String dateRange;
    private String language;
}
