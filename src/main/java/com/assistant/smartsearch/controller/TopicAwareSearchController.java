package com.assistant.smartsearch.controller;

import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.domain.model.SimplifiedSearchResult;
import com.assistant.smartsearch.application.TopicAwareSearchApplicationService;
import com.assistant.smartsearch.infrastructure.service.DatabaseLDATrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for topic-aware search functionality.
 * Provides endpoints for semantic search across documents with topic modeling.
 */
@Tag(name = "Topic-Aware Search", description = "Endpoints for semantic search with topic modeling")
@Validated
@RestController
@RequestMapping("/api")
public class TopicAwareSearchController {
    private static final Logger log = LoggerFactory.getLogger(TopicAwareSearchController.class);
    private final TopicAwareSearchApplicationService topicAwareSearchApplicationService;
    private final DatabaseLDATrainingService databaseLDATrainingService;

    @Autowired
    public TopicAwareSearchController(
            TopicAwareSearchApplicationService topicAwareSearchApplicationService,
            DatabaseLDATrainingService databaseLDATrainingService) {
        this.topicAwareSearchApplicationService = topicAwareSearchApplicationService;
        this.databaseLDATrainingService = databaseLDATrainingService;
    }

    /**
     * Perform a topic-aware semantic search across all available documents.
     *
     * @param request The search request containing the query and optional parameters
     * @return Search results with related documents and metadata
     */
    @Operation(
        summary = "Perform a topic-aware semantic search",
        description = "Searches across all documents using semantic understanding and topic modeling to find the most relevant results.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Search completed successfully",
                content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = SearchResult.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"
            )
        }
    )
    @PostMapping("/chatbot")
    public ResponseEntity<Map<String, Object>> search(
            @Valid @RequestBody(required = false) SearchRequest request,
            HttpServletRequest httpRequest) {

        // Validate request
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        log.info("Received topic-aware search request for query: {}", request.getQuery());

        long startTime = System.currentTimeMillis();

        // Perform the search
        List<SearchResult> searchResults = topicAwareSearchApplicationService.topicAwareSearch(request);

        // Convert to simplified results containing only the essential fields
        List<SimplifiedSearchResult> results = searchResults.stream()
                .map(SimplifiedSearchResult::from)
                .collect(Collectors.toList());

        // Prepare response with metadata
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("count", results.size());
        response.put("query", request.getQuery());
        response.put("tablesSearched", request.getTableName() == null ?
                "all" : request.getTableName());
        response.put("processingTimeMs", System.currentTimeMillis() - startTime);

        log.info("Search completed in {} ms with {} results",
                response.get("processingTimeMs"), results.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Train the LDA model with documents from a specific table
     *
     * @param tableName Name of the table to fetch documents from
     * @return Response with training status
     */
    @Operation(
        summary = "Train LDA topic model from database",
        description = "Fetches all documents from the specified table and trains the LDA model. This is an async operation."
    )
    @PostMapping("/model/train")
    public ResponseEntity<Map<String, Object>> trainModel(
            @RequestParam String tableName) {
        log.info("Received request to train LDA model from table: {}", tableName);

        long startTime = System.currentTimeMillis();

        // Train model asynchronously
        databaseLDATrainingService.trainModelFromDatabase(tableName);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Model training started");
        response.put("table", tableName);
        response.put("message", "LDA model is being trained asynchronously. Check /api/topics/model/status for progress.");
        response.put("processingTimeMs", System.currentTimeMillis() - startTime);

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get the status of the LDA model
     *
     * @return Model metadata and discovered topics
     */
    @Operation(
        summary = "Get LDA model status",
        description = "Returns information about the trained LDA model including discovered topics and vocabulary size."
    )
    @GetMapping("/model/status")
    public ResponseEntity<Map<String, Object>> getModelStatus() {
        log.info("Retrieving LDA model status");

        Map<String, Object> status = databaseLDATrainingService.getModelStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Clear the trained model
     *
     * @return Response confirming model was cleared
     */
    @Operation(
        summary = "Clear the trained LDA model",
        description = "Removes the currently trained model from memory."
    )
    @DeleteMapping("/model")
    public ResponseEntity<Map<String, Object>> clearModel() {
        log.info("Clearing LDA model");

        databaseLDATrainingService.clearModel();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Model cleared successfully");

        return ResponseEntity.ok(response);
    }

}
