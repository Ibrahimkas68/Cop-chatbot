package com.assistant.smartsearch.domain.port;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;

import java.util.List;

public interface SearchRepository {
    List<SearchResult> search(SearchRequest request);
    List<SearchResult> searchInTable(SearchRequest request, String tableName);
    List<SearchResult> fetchAllDocuments(String tableName, int size);
}