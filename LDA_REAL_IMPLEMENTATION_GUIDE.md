# 🎯 Real LDA Topic Modeling Implementation Guide

## Overview

Your search system now uses **real Latent Dirichlet Allocation (LDA)** topic modeling instead of hardcoded semantic groups. The model **learns from your actual database content** to discover topics automatically.

### What Changed

**Before (Hardcoded):**
```
"defence" → lookup in predefined dictionary → [security, protection, cyber...]
```

**Now (Real LDA):**
```
Documents from DB → Train LDA model → Discover topics automatically
                 ↓
"defence" search → Use discovered topics → Find similar documents
```

---

## 🚀 Getting Started

### Step 1: Train the Model from Your Database

```bash
POST /api/topics/model/train?tableName=actualities

# Response:
{
  "status": "Model training started",
  "table": "actualities",
  "message": "LDA model is being trained asynchronously. Check /api/topics/model/status for progress.",
  "processingTimeMs": 145
}
```

**What happens:**
1. All documents from your `actualities` table are fetched
2. Content is extracted (title, description, highlighted content)
3. LDA model trains on all documents (takes 30-60 seconds depending on document count)
4. Topics are automatically discovered

### Step 2: Check Model Status

```bash
GET /api/topics/model/status

# Response:
{
  "trained": true,
  "numDocuments": 25,
  "numTopics": 10,
  "vocabularySize": 1547,
  "topics": {
    "0": ["security", "attack", "defence", "protection", "cyber", "threat"],
    "1": ["data", "privacy", "encryption", "confidential", "information"],
    "2": ["network", "internet", "connection", "wireless", "communication"],
    "3": ["learning", "education", "training", "study", "course"],
    ...
  }
}
```

**What this means:**
- **numDocuments**: 25 documents were used to train (need at least 10+)
- **vocabularySize**: 1547 unique terms learned
- **topics**: Shows top 10 words for each discovered topic
- These topics are discovered **from your data**, not predefined!

### Step 3: Use Topic-Aware Search

```bash
POST /api/topics/search

{
  "query": "defence",
  "tableName": "actualities",
  "searchFields": ["title", "content"]
}

# Response:
{
  "results": [
    {
      "id": "1",
      "title": "Defence Strategies",
      "score": 0.95
    },
    {
      "id": "5",
      "title": "Cybersecurity Best Practices",  // ← Found because topic 0 includes both
      "score": 0.88
    },
    {
      "id": "8",
      "title": "Network Security Fundamentals",  // ← Related topic
      "score": 0.76
    }
  ],
  "count": 3,
  "query": "defence",
  "processingTimeMs": 245
}
```

---

## 🔄 How It Works

### The LDA Workflow

```
1. FETCH DOCUMENTS
   └─ Fetch all from your database
   
2. PREPROCESS TEXT
   ├─ Lowercase
   ├─ Tokenize
   ├─ Remove stop words
   └─ Create bag of words
   
3. TRAIN LDA MODEL
   ├─ Process 1000 iterations
   ├─ Use 10 topics
   ├─ Use 4 parallel threads
   └─ Discover hidden topics in your data
   
4. STORE MODEL IN MEMORY
   └─ Ready for fast inference
   
5. TOPIC-AWARE SEARCH
   ├─ Get query topic distribution
   ├─ Get document topic distributions
   ├─ Calculate similarity (cosine)
   ├─ Re-rank results
   └─ Return sorted by relevance
```

### Example: What LDA Discovered

If your database has documents about:
- "Defence mechanisms"
- "Security protocols"
- "Cybersecurity threats"
- "Network protection"
- "Data privacy"
- "Learning resources"

**LDA discovers** that documents 1-4 share similar topics while document 5 is different. So when you search "defence", it returns documents 1-4 because they share the same topic distribution, even if they use different words.

---

## ⚙️ Configuration

### Model Parameters

Edit `LDATopicModelService.java` to change:

```java
private static final int NUM_TOPICS = 10;           // Change number of topics
private static final int NUM_ITERATIONS = 1000;     // More iterations = better model
private static final int NUM_THREADS = 4;           // Parallel processing
private static final int TOP_WORDS_PER_TOPIC = 10;  // Words to display per topic
```

### Recommendation
- **NUM_TOPICS**: 
  - Small dataset (10-50 docs): 5 topics
  - Medium (50-500 docs): 10 topics
  - Large (500+ docs): 15-20 topics

- **NUM_ITERATIONS**:
  - Fast: 500 (lower quality)
  - Standard: 1000 (balanced)
  - Slow/Best: 2000 (better quality)

---

## 🛠️ API Endpoints

### 1. Train Model
```
POST /api/topics/model/train?tableName=actualities
```
- **Async operation** - Returns immediately
- Trains in background
- Check status endpoint to monitor progress

### 2. Get Model Status
```
GET /api/topics/model/status
```
- Returns model metadata
- Shows discovered topics
- Use to verify training completed

### 3. Perform Topic-Aware Search
```
POST /api/topics/search
Content-Type: application/json

{
  "query": "your search term",
  "tableName": "actualities",
  "searchFields": ["title", "content"]
}
```
- Uses LDA to find semantically similar documents
- Falls back to standard search if model not trained

### 4. Clear Model
```
DELETE /api/topics/model
```
- Removes model from memory
- Use before retraining with new data
- Frees up memory

---

## 📊 Example Usage Flow

```bash
# 1. Train model from your database
curl -X POST "http://localhost:8080/api/topics/model/train?tableName=actualities"

# Wait 30-60 seconds for training...

# 2. Check status
curl -X GET "http://localhost:8080/api/topics/model/status"

# 3. Once trained, search for related topics
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "defence",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'

# 4. Verify results include semantically similar documents
```

---

## ✅ Best Practices

### 1. Sufficient Training Data
- **Minimum**: 10 documents
- **Recommended**: 20+ documents
- **Optimal**: 100+ documents

### 2. Document Quality
- Remove very short documents (< 20 words)
- Clean text (remove noise/special characters)
- Use consistent terminology

### 3. Retraining
- Retrain when you add significant new content (50+ new docs)
- Clear model before retraining
- Monitor training logs for errors

### 4. Performance
- First search takes longer (model inference)
- Subsequent searches cached in memory
- Topic inference happens per query (not pre-computed)

---

## 🔍 How It Differs from Hardcoded Approach

| Aspect | Hardcoded | Real LDA |
|--------|-----------|----------|
| **Topics** | Predefined | Discovered from data |
| **Adaptability** | Fixed | Learns your data |
| **Accuracy** | Limited | Better with more data |
| **Flexibility** | Rigid | Discovers new relationships |
| **Training** | None | Required on DB content |
| **Maintenance** | Manual updates | Automatic |

---

## 🚨 Troubleshooting

### Model not training
**Check:**
- Database has documents: `SELECT COUNT(*) FROM actualities;`
- Logs show "LDA model training completed"
- Wait 60+ seconds for training to finish

### Search returns no results
**Reasons:**
- Model not trained yet (check `/api/topics/model/status`)
- Query has no related documents (check with standard search first)
- Empty database

**Solution:**
```bash
# 1. Check model status
curl -X GET "http://localhost:8080/api/topics/model/status"

# 2. If status is "No model trained yet":
curl -X POST "http://localhost:8080/api/topics/model/train?tableName=actualities"

# 3. Wait and try again
```

### Low relevance scores
- Increase NUM_ITERATIONS in LDATopicModelService
- Add more documents to database
- Ensure documents are related to your queries

---

## 📈 Real-World Example

### Your Database Contains:
```
1. "Cloud Security Best Practices"
2. "Network Defence Mechanisms"
3. "Cybersecurity Training Course"
4. "Data Privacy Regulations"
5. "Machine Learning Basics"
```

### Topics Discovered:
```
Topic 0: [security, defence, cyber, protection, attack, threat]
Topic 1: [data, privacy, personal, confidential, protection]
Topic 2: [learning, training, education, course, study]
Topic 3: [network, connection, internet, communication]
Topic 4: [regulation, compliance, rules, requirements, legal]
```

### Search: "defence"
**Query Distribution**: 80% Topic 0, 10% Topic 4, 10% Topic 1

**Results**:
1. Doc 2 (90% Topic 0) - **Exact match**
2. Doc 1 (85% Topic 0) - **Related: security**
3. Doc 3 (70% Topic 0) - **Related: cybersecurity/training**

---

## 🎉 You Now Have Real LDA!

**Key Improvements:**
- ✅ Discovers topics from YOUR data, not predefined dictionaries
- ✅ Scales with more documents - gets better over time
- ✅ No manual maintenance of semantic groups
- ✅ True statistical topic modeling using MALLET library
- ✅ Fast inference for topic-aware search

Start training your model and enjoy semantic search powered by real LDA!
