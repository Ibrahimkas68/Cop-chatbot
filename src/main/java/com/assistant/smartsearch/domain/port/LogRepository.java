package com.assistant.smartsearch.domain.port;

import com.assistant.smartsearch.domain.model.SearchLog;

import java.util.List;

public interface LogRepository {
    void save(SearchLog searchLog);
    List<SearchLog> findAll();
}