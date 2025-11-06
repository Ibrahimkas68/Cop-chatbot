package com.assistant.smartsearch.controller;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.model.SimplifiedSearchResult;
import com.assistant.smartsearch.domain.port.SearchUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/smart-search")
public class SmartSearchController {

    private final SearchUseCase searchUseCase;

    @Autowired
    public SmartSearchController(SearchUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
    }

    /**
     * Searches in a specific table or across all tables if no table is specified
     */
    @PostMapping
    public ResponseEntity<List<SimplifiedSearchResult>> search(@RequestBody SearchRequest request) {
        List<SearchResult> searchResults = searchUseCase.execute(request);
        List<SimplifiedSearchResult> results = searchResults.stream()
                .map(SimplifiedSearchResult::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
    
    /**
     * Explicitly searches across all available tables
     */
    @PostMapping("/all-tables")
    public ResponseEntity<List<SimplifiedSearchResult>> searchAllTables(@RequestBody SearchRequest request) {
        // Force search across all tables by setting tableName to null
        request.setTableName(null);
        List<SearchResult> searchResults = searchUseCase.execute(request);
        List<SimplifiedSearchResult> results = searchResults.stream()
                .map(SimplifiedSearchResult::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
}
