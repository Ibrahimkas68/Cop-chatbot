package com.assistant.smartsearch.domain.port;

import java.util.List;

public interface SemanticSimilarityPort {
    List<String> expandQuery(String query);
}