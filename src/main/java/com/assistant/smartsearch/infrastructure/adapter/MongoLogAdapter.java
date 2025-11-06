package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.model.SearchLog;
import com.assistant.smartsearch.domain.port.LogRepository;
import com.assistant.smartsearch.repository.SearchLogRepository; // Original repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoLogAdapter implements LogRepository {

    private final SearchLogRepository searchLogRepository;

    @Autowired
    public MongoLogAdapter(SearchLogRepository searchLogRepository) {
        this.searchLogRepository = searchLogRepository;
    }

    @Override
    public void save(SearchLog searchLog) {
        searchLogRepository.save(searchLog);
    }

    @Override
    public List<SearchLog> findAll() {
        return searchLogRepository.findAll();
    }
}