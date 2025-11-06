# 🌐 Semantic Search Implementation

## 📌 Table of Contents
- [Overview](#-overview)
- [Architecture](#-architecture)
  - [Core Components](#1-core-components)
- [How It Works](#-how-it-works)
  - [Query Processing Flow](#1-query-processing-flow)
  - [Semantic Groups](#2-semantic-groups)
- [Implementation Details](#-implementation-details)
- [Testing](#-testing)
- [Maintenance](#-maintenance)
- [Related Files](#-related-files)
- [Version History](#-version-history)

## 📌 Overview
This document provides comprehensive documentation of the semantic search functionality that enables finding related documents using word relationships (e.g., "defence" → "security").

## 🏗️ Architecture

### 1. Core Components

#### SemanticSimilarityService
- **Purpose**: Maps related terms across multiple languages
- **Location**: `src/main/java/com/assistant/smartsearch/service/SemanticSimilarityService.java`
- **Key Features**:
  - Predefined semantic groups (security, network, data, learning)
  - Multi-language support (English, French, Arabic)
  - Normalization of terms (case-insensitive, accent-insensitive)

#### TopicAwareSearchService
- **Purpose**: Handles search with semantic expansion
- **Location**: `src/main/java/com/assistant/smartsearch/service/TopicAwareSearchService.java`
- **Key Features**:
  - Expands search queries with related terms
  - Integrates with existing search infrastructure
  - Maintains backward compatibility

## 🔍 How It Works

### 1. Query Processing Flow

1. **Query Expansion**:
   ```java
   // Example: "defence" becomes "defence defense défense security securite sécurité protection..."
   List<String> expandedTerms = semanticSimilarity.expandQuery(request.getQuery());
   String expandedQuery = String.join(" ", expandedTerms);
   ```

2. **Search Execution**:
   - Uses the expanded query to search the database
   - Returns documents matching any of the related terms

### 2. Semantic Groups

#### Security Group
```java
Set<String> securityGroup = new HashSet<>(Arrays.asList(
    "security", "securite", "sécurité", "seguridad",
    "defence", "defense", "défense", "defensa",
    "protection", "protect", "protéger", "proteger",
    "cybersecurity", "cybersécurité", "cyber",
    "safety", "sûreté", "safe", "sécurisé",
    "guard", "guardian", "gardien",
    "shield", "bouclier",
    "firewall", "pare-feu",
    "encryption", "chiffrement", "cryptage",
    "attack", "attaque", "threat", "menace"
));
```

#### Learning Group
```java
Set<String> learningGroup = new HashSet<>(Arrays.asList(
    "learn", "apprendre", "aprender", "learning",
    "education", "éducation", "educación",
    "training", "formation", "formación",
    "teach", "enseigner", "enseñar",
    "study", "étudier", "estudiar",
    "course", "cours", "curso",
    "guide", "tutorial", "tutoriel"
));
```

## 🛠️ Implementation Details

### Key Methods

1. **expandQuery(String query)**
   - Splits query into terms
   - Finds related terms for each word
   - Returns combined list of all terms

2. **areRelated(String term1, String term2)**
   - Checks if two terms are in the same semantic group
   - Handles normalization and language variations

3. **normalize(String term)**
   - Converts to lowercase
   - Removes diacritics
   - Trims whitespace

## 🧪 Testing

### Test Script
```bash
./test_semantic_search.sh
```

### Test Cases

1. **Basic Search**
   ```bash
   # Search "defence" finds "security" documents
   curl -X POST "http://localhost:8080/api/topics/search" \
     -H "Content-Type: application/json" \
     -d '{"query": "defence", "tableName": "actualities"}'
   ```

2. **Multi-word Query**
   ```bash
   # Search "apprendre la securite" finds learning and security docs
   curl -X POST "http://localhost:8080/api/topics/search" \
     -H "Content-Type: application/json" \
     -d '{"query": "apprendre la securite", "tableName": "actualities"}'
   ```

## 📈 Benefits

1. **No Training Required**
   - Works immediately with predefined term relationships
   - No need for machine learning model training

2. **Multi-language Support**
   - Handles English, French, and Arabic terms
   - Easy to add more languages

3. **Extensible**
   - New semantic groups can be added easily
   - Simple to update term relationships

## 🔧 Maintenance

### Adding New Terms
1. Edit `SemanticSimilarityService.java`
2. Add terms to existing groups or create new groups
3. No server restart needed for changes to take effect

### Performance
- In-memory term mapping for fast lookups
- Minimal overhead during query expansion

## 📚 Related Files

1. `src/main/java/com/assistant/smartsearch/service/SemanticSimilarityService.java` - Core semantic logic
2. `src/main/java/com/assistant/smartsearch/service/TopicAwareSearchService.java` - Search integration
3. `test_semantic_search.sh` - Test script
4. `demo_topic_search.sh` - Demo script

## 📅 Version History

- **1.0.0** (2025-10-23)
  - Initial implementation
  - Support for security and learning domains
  - Multi-language support (EN/FR/AR)

## 👨‍💻 Development Notes

### Adding New Semantic Groups
1. Add a new `Set<String>` constant in `SemanticSimilarityService`
2. Add it to the `SEMANTIC_GROUPS` map
3. Test with relevant search terms

### Testing New Terms
```bash
# Test if two terms are related
curl -X GET "http://localhost:8080/api/topics/related?term1=defence&term2=security"

# See all related terms for a word
curl -X GET "http://localhost:8080/api/topics/related/defence"
```

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
