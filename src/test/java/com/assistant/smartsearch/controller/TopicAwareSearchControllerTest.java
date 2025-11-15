package com.assistant.smartsearch.controller;

import com.assistant.smartsearch.application.TopicAwareSearchApplicationService;
import com.assistant.smartsearch.domain.model.SearchRequest;
import com.assistant.smartsearch.domain.model.SearchResult;
import com.assistant.smartsearch.infrastructure.service.DatabaseLDATrainingService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicAwareSearchControllerTest {

    @Mock
    private TopicAwareSearchApplicationService topicAwareSearchApplicationService;

    @Mock
    private DatabaseLDATrainingService databaseLDATrainingService;

    @InjectMocks
    private TopicAwareSearchController topicAwareSearchController;

    private SearchRequest testRequest;
    private List<SearchResult> testResults;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testRequest = new SearchRequest();
        testRequest.setQuery("test query");
        testRequest.setTableName("test_table");

        SearchResult result1 = new SearchResult();
        result1.setId("doc1");
        result1.setTitle("Test Document 1");

        SearchResult result2 = new SearchResult();
        result2.setId("doc2");
        result2.setTitle("Related Document 1");

        testResults = Arrays.asList(result1, result2);
    }

    @Test
    void search_ShouldReturnSearchResults() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("results", testResults);
        responseBody.put("count", testResults.size());
        responseBody.put("query", testRequest.getQuery());
        responseBody.put("tablesSearched", "all");
        responseBody.put("processingTimeMs", 100L);

        when(topicAwareSearchApplicationService.topicAwareSearch(any(SearchRequest.class)))
            .thenReturn(testResults);

        // Act
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ResponseEntity<Map<String, Object>> response = topicAwareSearchController.search(testRequest, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBodyResult = response.getBody();
        assertNotNull(responseBodyResult);

        @SuppressWarnings("unchecked")
        List<SearchResult> results = (List<SearchResult>) responseBodyResult.get("results");
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getId());

        verify(topicAwareSearchApplicationService, times(1)).topicAwareSearch(testRequest);
    }

    @Test
    void search_WithNullRequest_ShouldReturnBadRequest() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            topicAwareSearchController.search(null, mockRequest);
        });
    }

    @Test
    void search_WithEmptyResults_ShouldReturnEmptyList() {
        // Arrange
        when(topicAwareSearchApplicationService.topicAwareSearch(any(SearchRequest.class)))
            .thenReturn(List.of());

        // Act
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ResponseEntity<Map<String, Object>> response = topicAwareSearchController.search(testRequest, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);

        @SuppressWarnings("unchecked")
        List<SearchResult> results = (List<SearchResult>) responseBody.get("results");
        assertTrue(results.isEmpty());

        verify(topicAwareSearchApplicationService, times(1)).topicAwareSearch(testRequest);
    }
}
