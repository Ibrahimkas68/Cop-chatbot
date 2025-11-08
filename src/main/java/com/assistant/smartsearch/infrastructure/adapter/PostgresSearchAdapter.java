package com.assistant.smartsearch.infrastructure.adapter;

import com.assistant.smartsearch.domain.port.SearchRepository;
import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.model.Performance;
import com.assistant.smartsearch.domain.model.SearchLog;
import com.assistant.smartsearch.domain.model.SearchResultLog;
import com.assistant.smartsearch.domain.port.KeywordExtractor; // Added import
import com.assistant.smartsearch.domain.port.ScoringEngine; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostgresSearchAdapter implements SearchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final KeywordExtractor keywordExtractor;
    private final ScoringEngine scoringEngine;

    public PostgresSearchAdapter(JdbcTemplate jdbcTemplate,
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                 KeywordExtractor keywordExtractor,
                                 ScoringEngine scoringEngine) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.keywordExtractor = keywordExtractor;
        this.scoringEngine = scoringEngine;
    }

    @Override
    public List<SearchResult> search(SearchRequest request) {
        if (request.getTableName() == null || request.getTableName().isEmpty()) {
            throw new IllegalArgumentException("Table name must be specified for single table search.");
        }
        return searchInTable(request, request.getTableName());
    }
    
    /**
     * Searches within a specific table
     */
    public List<SearchResult> searchInTable(SearchRequest request, String tableName) {
        long startTime = System.currentTimeMillis();

        // Extract meaningful keywords from the query
        List<String> keywords = keywordExtractor.extractKeywords(request.getQuery());
        System.out.println("Extracted keywords: " + keywords);

        // Create a copy of the request with the current table name
        SearchRequest tableRequest = new SearchRequest();
        tableRequest.setQuery(request.getQuery());
        tableRequest.setTableName(tableName);
        tableRequest.setSearchFields(request.getSearchFields());
        tableRequest.setPage(request.getPage());
        tableRequest.setSize(request.getSize());

        // Determine actual fields to search using the table-specific request
        List<String> actualFieldsToSearch = determineSearchFields(tableRequest);
        System.out.println("Searching in table " + tableName + " with fields: " + actualFieldsToSearch);

        // Build and execute search query
        List<Map<String, Object>> rawResults = executeSearch(
                tableName, actualFieldsToSearch, keywords);

        // Score and sort results
        List<SearchResult> scoredResults = scoringEngine.scoreAndSort(
                rawResults, keywords, actualFieldsToSearch, request.getQuery(), tableName);

        // Apply size limit to scoredResults
        int limit = request.getSize();
        if (limit > 0 && scoredResults.size() > limit) {
            scoredResults = scoredResults.subList(0, limit);
        }

        long endTime = System.currentTimeMillis();

        return scoredResults;
    }
    
    /**
     * Determines which fields to search in.
     * Uses provided searchFields or auto-detects from database schema.
     */
    private List<String> determineSearchFields(SearchRequest request) {
        List<String> searchFields = request.getSearchFields();
        
        if (searchFields == null || searchFields.isEmpty()) {
            searchFields = getAllSearchableFields(request.getTableName());
            System.out.println("Auto-detected fields for table " + request.getTableName() + ": " + searchFields);
        }
        
        return searchFields;
    }

    private List<Map<String, Object>> executeSearch(
            String tableName, List<String> fields, List<String> keywords) {

        if (keywords.isEmpty() || fields.isEmpty()) {
            System.out.println("No keywords or fields to search, returning empty results");
            return new ArrayList<>();
        }

        // Build the query and parameters
        Map<String, Object> params = new HashMap<>();
        String sql = buildSearchQuery(tableName, fields, keywords, params);
        System.out.println("Generated SQL: " + sql);

        try {
            // Use NamedParameterJdbcTemplate for safe query execution
            return namedParameterJdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            System.err.println("Error executing search query: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String buildSearchQuery(String tableName, List<String> fields, List<String> keywords, Map<String, Object> params) {
        // Sanitize table and field names to prevent SQL injection from identifiers
        String sanitizedTableName = sanitizeIdentifier(tableName);
        List<String> sanitizedFields = fields.stream()
                .map(this::sanitizeIdentifier)
                .collect(Collectors.toList());

        StringBuilder sql = new StringBuilder("SELECT * FROM ")
                .append(sanitizedTableName)
                .append(" WHERE ");

        List<String> conditions = new ArrayList<>();
        int keywordIndex = 0;
        for (String keyword : keywords) {
            String keywordParam = "keyword" + keywordIndex++;
            params.put(keywordParam, "%" + keyword.toLowerCase() + "%");
            for (String field : sanitizedFields) {
                conditions.add(String.format("LOWER(%s) ILIKE :%s", field, keywordParam));
            }
        }

        sql.append(String.join(" OR ", conditions));
        return sql.toString();
    }

    private String sanitizeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        // Allow only alphanumeric characters and underscores
        if (!identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
        return identifier;
    }

    /**
     * Retrieves all searchable text fields from the specified table.
     * Queries the database schema to find text-based columns.
     * Excludes non-content fields like URLs, status, language, etc.
     */
    private List<String> getAllSearchableFields(String tableName) {
        // Handle null or empty table name
        if (tableName == null || tableName.trim().isEmpty()) {
            System.err.println("Table name is null or empty");
            return new ArrayList<>();
        }
        
        // Common fields to exclude from search
        Set<String> excludedFields = new HashSet<>(Arrays.asList(
            "id", "created_at", "updated_at", "status", "is_active", "version",
            "author_id", "category_id", "user_id", "image_url", "thumbnail"
        ));
        
        try {
            // Query information_schema to get ONLY text-based columns
            String sql = "SELECT column_name FROM information_schema.columns " +
                       "WHERE table_name = ? " +
                       "AND table_schema = 'public' " +
                       "AND data_type IN ('character varying', 'varchar', 'text', 'char', 'character') " +
                       "ORDER BY column_name";
            
            String normalizedTableName = tableName.toLowerCase();
            System.out.println("Querying schema for table: " + normalizedTableName);
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, normalizedTableName);
            
            if (!columns.isEmpty()) {
                List<String> fields = columns.stream()
                        .map(col -> (String) col.get("column_name"))
                        .filter(col -> col != null && !col.isEmpty())
                        .filter(col -> !excludedFields.contains(col.toLowerCase()))
                        .collect(Collectors.toList());
                
                System.out.println("Found " + columns.size() + " text columns, filtered to " + 
                                 fields.size() + " searchable fields: " + fields);
                return fields;
            } else {
                System.out.println("No text columns found for table: " + tableName);
            }
        } catch (Exception e) {
            String errorMsg = String.format("Failed to query schema for table '%s': %s", 
                tableName, e.getMessage());
            System.err.println(errorMsg);
            if (!(e instanceof NullPointerException)) {
                e.printStackTrace();
            }
        }
        
        System.err.println("Returning empty list for table: " + tableName);
        return new ArrayList<>();
    }

}