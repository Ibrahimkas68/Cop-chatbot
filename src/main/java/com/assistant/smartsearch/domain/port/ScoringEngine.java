package com.assistant.smartsearch.domain.port;

import com.assistant.smartsearch.domain.model.SearchResult;
import java.util.List;
import java.util.Map;

public interface ScoringEngine {
    List<SearchResult> scoreAndSort(
            List<Map<String, Object>> rawResults,
            List<String> keywords,
            List<String> searchFields,
            String query,
            String tableName);
}