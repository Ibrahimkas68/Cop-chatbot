package com.assistant.smartsearch.application;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.port.SearchRepository;
import com.assistant.smartsearch.infrastructure.service.LDATopicModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicAwareSearchApplicationService {

    private final SearchRepository searchRepository;
    private final LDATopicModelService ldaService;

    @Autowired
    public TopicAwareSearchApplicationService(SearchRepository searchRepository,
                                            LDATopicModelService ldaService) {
        this.searchRepository = searchRepository;
        this.ldaService = ldaService;
    }

    /**
     * Perform topic-aware search using real LDA topic modeling
     */
    public List<SearchResult> topicAwareSearch(SearchRequest request) {
        // 1. Validate request
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        log.info("Starting LDA-based topic-aware search for query: {}", request.getQuery());

        // 2. Check if model is trained
        if (!ldaService.isModelTrained()) {
            log.warn("LDA model not trained yet, falling back to standard search");
            List<SearchResult> fallbackResults = new ArrayList<>();
            String[] tablesToSearch = {"actualities", "initiatives", "glossary"};
            for (String table : tablesToSearch) {
                fallbackResults.addAll(searchRepository.searchInTable(request, table));
            }
            return fallbackResults;
        }

        // 3. Infer topics for the query
        double[] queryTopics = ldaService.inferDocumentTopics(request.getQuery());

        // 4. Fetch all documents from the database for topic comparison
        List<SearchResult> allDocuments = new ArrayList<>();
        String[] tablesToSearch = {"actualities", "initiatives", "glossary"};
        for (String table : tablesToSearch) {
            allDocuments.addAll(searchRepository.fetchAllDocuments(table, 10000)); // Fetch all documents from each table
        }

        // 5. Calculate similarity for each document and re-rank
        List<SearchResult> scoredResults = allDocuments.stream()
                .map(doc -> {
                    // Assuming description holds the main content for topic inference
                    String contentForInference = doc.getDescription() != null ? doc.getDescription() : "";
                    if (doc.getTitle() != null && !doc.getTitle().isEmpty()) {
                        contentForInference = doc.getTitle() + " " + contentForInference;
                    }

                    double[] docTopics = ldaService.inferDocumentTopics(contentForInference);
                    double topicSimilarity = ldaService.calculateTopicDistributionSimilarity(queryTopics, docTopics);
                    doc.setScore(topicSimilarity); // Set score based on topic similarity
                    return doc;
                })
                .filter(doc -> doc.getScore() > 0.1) // Filter out low similarity scores
                .collect(Collectors.toList()); // Collect all scored results first

        // Group by table, sort by score, and take top 3 from each table
        Map<String, List<SearchResult>> groupedByTable = scoredResults.stream()
                .collect(Collectors.groupingBy(SearchResult::getTableName));

        List<SearchResult> topResultsPerTable = new ArrayList<>();
        for (Map.Entry<String, List<SearchResult>> entry : groupedByTable.entrySet()) {
            entry.getValue().stream()
                    .sorted((d1, d2) -> Double.compare(d2.getScore(), d1.getScore()))
                    .limit(3)
                    .forEach(topResultsPerTable::add);
        }

        // Sort the final combined list by score (optional, but good for overall presentation)
        topResultsPerTable.sort((d1, d2) -> Double.compare(d2.getScore(), d1.getScore()));

        log.info("Topic-aware search completed with {} results (top 3 per table)", topResultsPerTable.size());
        return topResultsPerTable;
    }
}
