package com.assistant.smartsearch.repository;

import com.assistant.smartsearch.domain.model.SearchLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SearchLogRepository extends MongoRepository<SearchLog, String> {
}
