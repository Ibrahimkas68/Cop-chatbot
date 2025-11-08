package com.assistant.smartsearch.application;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.port.SearchUseCase;
import com.assistant.smartsearch.infrastructure.service.LDATopicModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TopicAwareSearchApplicationService {

    private final SearchUseCase searchUseCase;
    private final LDATopicModelService ldaService;

    @Autowired
    public TopicAwareSearchApplicationService(SearchUseCase searchUseCase,
                                            LDATopicModelService ldaService) {
        this.searchUseCase = searchUseCase;
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
            return searchUseCase.execute(request);
        }
        
        // 3. Get initial search results
        List<SearchResult> initialResults = searchUseCase.execute(request);
        
        // 4. Get topic distribution of the query
        double[] queryTopics = ldaService.inferDocumentTopics(request.getQuery());
        
        // 5. Re-rank results by topic similarity
        List<SearchResult> rerankedResults = new ArrayList<>(initialResults);
        rerankedResults.forEach(result -> {
            String content = result.getHighlightedContent();
            if (content != null && !content.isEmpty()) {
                double topicSimilarity = ldaService.calculateDocumentSimilarity(
                    request.getQuery(),
                    content
                );
                // Boost score based on topic similarity
                if (result.getScore() != null) {
                    result.setScore(result.getScore() * (1 + topicSimilarity));
                } else {
                    result.setScore(topicSimilarity);
                }
            }
        });
        
        // 6. Sort by new score
        rerankedResults.sort((a, b) -> Double.compare(
            b.getScore() != null ? b.getScore() : 0,
            a.getScore() != null ? a.getScore() : 0
        ));
        
        log.info("Topic-aware search completed with {} results", rerankedResults.size());
        return rerankedResults;
    }
}
