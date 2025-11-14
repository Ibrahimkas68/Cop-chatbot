package com.assistant.smartsearch.application;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.model.SearchLog;
import com.assistant.smartsearch.domain.model.SearchResultLog;
import com.assistant.smartsearch.domain.model.Performance;
import com.assistant.smartsearch.domain.port.KeywordExtractor;
import com.assistant.smartsearch.domain.port.LogRepository;
import com.assistant.smartsearch.domain.port.SearchRepository;
import com.assistant.smartsearch.domain.port.ScoringEngine;
import com.assistant.smartsearch.domain.port.SearchUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchApplicationService implements SearchUseCase {

    private final KeywordExtractor keywordExtractor;
    private final SearchRepository searchRepository;
    private final ScoringEngine scoringEngine;
    private final LogRepository logRepository;

    public SearchApplicationService(KeywordExtractor keywordExtractor,
                                    SearchRepository searchRepository,
                                    ScoringEngine scoringEngine,
                                    LogRepository logRepository) {
        this.keywordExtractor = keywordExtractor;
        this.searchRepository = searchRepository;
        this.scoringEngine = scoringEngine;
        this.logRepository = logRepository;
    }

    @Override
    public List<SearchResult> execute(SearchRequest request) {
        // If no table name is specified, search across all tables
        if (request.getTableName() == null || request.getTableName().isEmpty()) {
            return searchAcrossAllTables(request);
        }
        
        return searchRepository.searchInTable(request, request.getTableName());
    }
    
    /**
     * Searches across all available tables and combines the results
     */
    private List<SearchResult> searchAcrossAllTables(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        List<SearchResult> allResults = new ArrayList<>();
        
        // Define all tables to search in
        String[] tablesToSearch = {"actualities", "initiatives","articles","advices","guides"};
        System.out.println("Searching across tables: " + Arrays.toString(tablesToSearch));
        
        // Store original size and page to restore them later
        int originalSize = request.getSize();
        int originalPage = request.getPage();

        // Set size to 3 for per-table search
        request.setSize(3);
        request.setPage(0); // Always get the first page (top 3) for each table

        // Search in each table and collect results
        for (String table : tablesToSearch) {
            try {
                System.out.println("Searching in table: " + table);
                List<SearchResult> tableResults = searchRepository.searchInTable(request, table);
                System.out.println("Found " + tableResults.size() + " results in table " + table);
                allResults.addAll(tableResults);
            } catch (Exception e) {
                System.err.println("Error searching in table " + table + ": " + e.getMessage());
                // Continue with other tables even if one fails
            }
        }

        // Restore original size and page for logging or further processing if needed
        request.setSize(originalSize);
        request.setPage(originalPage);
        
        System.out.println("Total results before final sorting: " + allResults.size());

        // Sort all results by score (highest first)
        allResults.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));

        // No further pagination needed here, as we already limited per table
        // The 'count' in the response should reflect the total number of results collected (e.g., 3*num_tables)

        // Extract keywords for logging
        List<String> keywords = keywordExtractor.extractKeywords(request.getQuery());
        
        // Log the search for analytics
        logSearch(request, keywords, allResults, allResults.size(), 
                 System.currentTimeMillis() - startTime);

        return allResults;
    }

    private void logSearch(SearchRequest request, List<String> keywords, 
                          List<SearchResult> scoredResults, int totalResults, long searchTimeMs) {
        try {
            SearchLog searchLog = new SearchLog();
            searchLog.setOriginalQuery(request.getQuery());
            searchLog.setExtractedKeywords(keywords);

            List<SearchResultLog> resultLogs = scoredResults.stream().map(r -> {
                SearchResultLog resultLog = new SearchResultLog();
                resultLog.setDocumentId(r.getOriginalId());
                resultLog.setTitle((String) r.getData().get("title"));
                resultLog.setScore(r.getScore());
                resultLog.setMatchedKeywords(r.getMatchedKeywords());
                resultLog.setPosition(scoredResults.indexOf(r) + 1);
                return resultLog;
            }).collect(Collectors.toList());
            searchLog.setResults(resultLogs);

            Performance performance = new Performance();
            performance.setSearchTimeMs(searchTimeMs);
            performance.setTotalResults(totalResults);
            performance.setResultsReturned(scoredResults.size());
            searchLog.setPerformance(performance);

            searchLog.setTimestamp(new Date());

            logRepository.save(searchLog);
        } catch (Exception e) {
            System.err.println("Error logging search: " + e.getMessage());
            // Don't fail the search if logging fails
        }
    }
}