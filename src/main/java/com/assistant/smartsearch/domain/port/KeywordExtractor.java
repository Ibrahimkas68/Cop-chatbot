package com.assistant.smartsearch.domain.port;

import java.util.List;

public interface KeywordExtractor {
    List<String> extractKeywords(String query);
}