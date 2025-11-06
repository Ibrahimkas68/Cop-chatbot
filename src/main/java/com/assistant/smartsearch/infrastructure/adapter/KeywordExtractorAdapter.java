package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.port.KeywordExtractor;
import com.assistant.smartsearch.config.StopWordsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class KeywordExtractorAdapter implements KeywordExtractor {

    private final StopWordsConfig stopWordsConfig;

    @Autowired
    public KeywordExtractorAdapter(StopWordsConfig stopWordsConfig) {
        this.stopWordsConfig = stopWordsConfig;
    }

    /**
     * Extracts meaningful keywords from a search query.
     * Removes stop words and short words, normalizes text.
     * 
     * @param query The search query string
     * @return List of extracted keywords
     */
    public List<String> extractKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Normalize and clean the query
        String normalizedQuery = query.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s\\u0600-\\u06FF]", " ") // Replace special chars with space
                .trim();

        // Split into words
        String[] words = normalizedQuery.split("\\s+");

        // Extract meaningful keywords
        List<String> keywords = Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .filter(word -> word.length() > 2 || stopWordsConfig.isImportantShortWord(word)) // Allow important short words
                .filter(word -> !stopWordsConfig.isStopWord(word)) // Filter out stop words
                .distinct() // Remove duplicates
                .collect(Collectors.toList());

        // Return keywords without excessive variations to keep queries manageable
        // The ILIKE operator will handle partial matches
        return keywords;
    }
}
