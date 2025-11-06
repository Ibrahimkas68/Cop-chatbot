package com.assistant.smartsearch.domain.port;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;

import java.util.List;

public interface SearchUseCase {
    List<SearchResult> execute(SearchRequest request);
}