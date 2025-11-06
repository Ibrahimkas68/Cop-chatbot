# Smart Search Service

A generic smart search service that provides a flexible and powerful search experience across your database tables, now refactored with a Clean Architecture approach.

## About The Project

This project provides a "smart" search API that can be configured to search across different tables and fields. It includes features like keyword extraction, semantic query expansion, result scoring, and search logging. The architecture has been refactored to adhere to Clean Architecture principles, promoting separation of concerns, testability, and maintainability.

### Built With

*   [Spring Boot](https://spring.io/projects/spring-boot)
*   [Maven](https://maven.apache.org/)
*   [PostgreSQL](https://www.postgresql.org/)
*   [MongoDB](https://www.mongodb.com/)
*   [Docker](https://www.docker.com/)

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

*   Java 17
*   Docker

### Installation

1.  Clone the repo
    ```sh
    git clone <your-repository-url>
    ```
2.  Build the project and Docker image
    ```sh
    ./mvnw clean install
    ```
3.  Start the services
    ```sh
    docker-compose up -d --build
    ```

## Usage

The API provides endpoints for performing smart searches.

### Endpoints

1.  **Smart Search (Single Table or All Tables)**
    `POST /api/smart-search`

    *   **Description**: Performs a smart search. If `tableName` is specified, it searches within that table. Otherwise, it searches across all configured tables.
    *   **Request Body**: 
        ```json
        {
          "query": "your search query",
          "tableName": "your_table_name", // Optional: if omitted, searches all tables
          "searchFields": ["field1", "field2"], // Optional: if omitted, auto-detects searchable fields
          "size": 20 // Optional: number of results to return (default 20)
        }
        ```
    *   **Example**: 
        ```bash
        curl -X POST http://localhost:8080/api/smart-search \
        -H "Content-Type: application/json" \
        -d '{
          "query": "example search",
          "tableName": "products",
          "searchFields": ["name", "description"],
          "size": 10
        }'
        ```

2.  **Topic-Aware Search (Semantic Search)**
    `POST /api/topics/search`

    *   **Description**: Performs a topic-aware semantic search. The query is expanded with semantically related terms before the search is executed across all configured tables. This endpoint now returns the top 3 scored objects from each table.
    *   **Request Body**: 
        ```json
        {
          "query": "your semantic search query",
          "searchFields": [], // Optional: if omitted, auto-detects searchable fields
          "size": 3 // Optional: number of results to return per table (default 3)
        }
        ```
    *   **Example**: 
        ```bash
        curl -X POST http://localhost:8080/api/topics/search \
        -H "Content-Type: application/json" \
        -d '{
          "query": "comment sensibiliser mes enfants contre le chantage ",
          "searchFields": []
        }'
        ```

### Response Body

The API returns a JSON object containing search results and metadata.

```json
{
  "processingTimeMs": 123,
  "query": "your search query",
  "tablesSearched": "all" | "your_table_name",
  "count": 5, // Total number of results returned in this response
  "results": [
    {
      "id": "unique_id",
      "originalId": 123,
      "query": "original query",
      "tableName": "table_name",
      "data": {
        "field1": "value1",
        "field2": "value2"
      },
      "score": 0.85,
      "matchedKeywords": ["keyword1", "keyword2"],
      "highlightedText": "Optional highlighted text",
      "totalResults": 100, // Total results found before pagination
      "pageNumber": 0,
      "pageSize": 20,
      "title": "Document Title",
      "url": "https://www.e-himaya.gov.ma/{tableName}/{slug}",
      "imageName": "{imageName}",
      "description": "Short description of the content",
      "metadata": {}
    }
  ]
}
```

### Error Handling

In case of an error, the API will return a standard Spring Boot error response.

## Architecture (Clean Architecture)

The application is designed following Clean Architecture principles, separating concerns into distinct layers:

*   **Domain Layer (`com.assistant.smartsearch.domain`)**:
    *   **Models (`.model`)**: Contains Plain Old Java Objects (POJOs) representing the core business entities and data structures (e.g., `SearchRequest`, `SearchResult`, `SearchLog`, `Performance`). These are independent of any framework or database.
    *   **Ports (`.port`)**: Defines interfaces (ports) that represent the application's business rules and interactions with external systems. These are contracts that the Application layer depends on, and the Infrastructure layer implements.
        *   `SearchUseCase`: Defines the core search operation.
        *   `SearchRepository`: Abstraction for data retrieval from various sources (e.g., PostgreSQL tables).
        *   `LogRepository`: Abstraction for logging search events.
        *   `KeywordExtractor`: Abstraction for extracting keywords from text.
        *   `ScoringEngine`: Abstraction for scoring search results.
        *   `SemanticSimilarityPort`: Abstraction for semantic query expansion.

*   **Application Layer (`com.assistant.smartsearch.application`)**:
    *   Contains the application-specific business rules and orchestrates the flow of data. It depends only on the Domain layer.
    *   `SearchApplicationService`: Implements `SearchUseCase`, coordinating the search process using `KeywordExtractor`, `SearchRepository`, `ScoringEngine`, and `LogRepository`.
    *   `TopicAwareSearchApplicationService`: Handles semantic query expansion using `SemanticSimilarityPort` and then delegates to `SearchUseCase`.
    *   `DocumentProcessorService`: Processes documents for keyword extraction and vectorization.

*   **Infrastructure Layer (`com.assistant.smartsearch.infrastructure`)**:
    *   Contains the implementations of the ports defined in the Domain layer. This layer deals with external concerns like databases, frameworks, and external APIs. It depends on the Application and Domain layers.
    *   **Adapters (`.adapter`)**: Implementations of the Domain ports.
        *   `PostgresSearchAdapter`: Implements `SearchRepository`, handling PostgreSQL database interactions using `JdbcTemplate`.
        *   `MongoLogAdapter`: Implements `LogRepository`, interacting with MongoDB via `SearchLogRepository`.
        *   `KeywordExtractorAdapter`: Implements `KeywordExtractor`, providing keyword extraction logic.
        *   `ScoringAdapter`: Implements `ScoringEngine`, providing result scoring logic.
        *   `SemanticSimilarityAdapter`: Implements `SemanticSimilarityPort`, providing semantic query expansion logic.
    *   **Repositories (`.repository`)**: Spring Data repositories (e.g., `SearchLogRepository` for MongoDB).

*   **Controller Layer (`com.assistant.smartsearch.controller`)**:
    *   Exposes the API endpoints. It depends on the Application layer (Use Cases).
    *   `SmartSearchController`: Exposes the `/api/smart-search` endpoint, delegating to `SearchApplicationService`.
    *   `TopicAwareSearchController`: Exposes the `/api/topics/search` endpoint, delegating to `TopicAwareSearchApplicationService`.

## Configuration

The application can be configured through the `application.properties` file or by setting environment variables.

### Database

*   `SPRING_DATASOURCE_URL`: The JDBC URL of your PostgreSQL database.
*   `SPRING_DATASOURCE_USERNAME`: The username for your PostgreSQL database.
*   `SPRING_DATASOURCE_PASSWORD`: The password for your PostgreSQL database.

### Logging

*   `SPRING_DATA_MONGODB_URI`: The connection string for your MongoDB database.

## Logging

The service logs detailed information about each search to a MongoDB collection named `search_logs`. This data can be used for:

*   **Analytics:** Understanding what users are searching for.
*   **Monitoring:** Tracking the performance of the search service.
*   **Improving Search Relevance:** Analyzing search patterns to fine-tune the scoring algorithm.

Each search log document contains:

*   The original query.
*   The extracted keywords.
*   The ranked list of results.
*   Performance metrics (e.g., search time).

## Extensibility

The service is designed to be highly extensible due to its Clean Architecture:

*   **Keyword Extraction:** Easily swap `KeywordExtractorAdapter` with a different implementation (e.g., using a more advanced NLP library) by implementing the `KeywordExtractor` interface.
*   **Scoring:** Customize the `ScoringAdapter` to implement your own scoring algorithm by implementing the `ScoringEngine` interface.
*   **Data Sources:** Adapt to different data sources (e.g., Elasticsearch, other SQL databases) by creating new implementations of the `SearchRepository` interface.
*   **Logging:** Change logging mechanisms (e.g., to a different NoSQL database or a log aggregation service) by implementing the `LogRepository` interface.
*   **Semantic Similarity:** Replace or enhance the `SemanticSimilarityAdapter` by implementing the `SemanticSimilarityPort` interface.

## Testing

To run the tests for this project, you can use the following Maven command:

```sh
./mvnw test
```

## Code Style

This project follows the standard Java code style.

## Security

This service is intended to be used as a backend service and does not include any authentication or authorization mechanisms out of the box. It is recommended to deploy it behind an API gateway or other security layer that can handle these concerns.

## Deployment

The application is containerized using Docker and can be deployed using the provided `docker-compose.yaml` file. This will start the application, a MongoDB database, and a PostgreSQL database.

Before deploying, make sure to configure the database connections in the `docker-compose.yaml` file.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## License

Copyright (c) 2025 ZenithSoft
