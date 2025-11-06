# ✅ SOLUTION: Search "Defence" → Get "Security" Documents

## 🎯 Your Goal
When searching for **"defence"**, you want it to also return documents about **"security"**, **"cybersecurity"**, and other related topics.

## ✅ How to Achieve This

### Use the Topic-Aware Search Endpoint
```bash
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "defence",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'
```

**NOT** the standard search:
```bash
# ❌ This only returns exact matches
curl -X POST "http://localhost:8080/api/smart-search" ...
```

---

## 🚀 Quick Start (3 Steps)

### Step 1: Make Sure You Have Data
Your `actualities` table needs documents about defence, security, cybersecurity, etc.

```sql
SELECT COUNT(*) FROM actualities;
-- Should return > 0
```

### Step 2: Train the Model
Run multiple searches to build the topic model:

```bash
./demo_topic_search.sh
```

Or manually:
```bash
# Search 1
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "security", "tableName": "actualities", "searchFields": ["title", "content"]}'

# Search 2
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}'

# Search 3
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "cybersecurity", "tableName": "actualities", "searchFields": ["title", "content"]}'

# Search 4
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "protection", "tableName": "actualities", "searchFields": ["title", "content"]}'
```

### Step 3: Test It!
```bash
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}' \
  | python3 -m json.tool
```

**Expected Result:**
```json
[
  {
    "id": "1",
    "title": "Defence Strategies",
    "score": 0.95
  },
  {
    "id": "5",
    "title": "Cybersecurity Best Practices",  // ← Related!
    "score": 0.82
  },
  {
    "id": "8",
    "title": "Network Security Fundamentals",  // ← Related!
    "score": 0.78
  }
]
```

---

## 🔍 How It Works

### The Magic: LDA Topic Modeling

1. **You search multiple times** → Documents get added to cache
2. **Model trains automatically** → Discovers hidden topics
3. **Topics group related terms:**
   - Topic 0: [defence, security, cyber, attack, protection]
   - Topic 1: [data, privacy, encryption, secure]
   - Topic 2: [network, firewall, infrastructure]

4. **When you search "defence":**
   - Finds direct matches (documents with "defence")
   - Finds similar documents (same topic distribution)
   - Returns both together!

---

## 📊 Comparison

### Standard Search (`/api/smart-search`)
```
Query: "defence"
Results: Only documents containing "defence"
```

### Topic-Aware Search (`/api/topics/search`) ✅
```
Query: "defence"
Results: 
  - Documents containing "defence"
  - Documents about "security" (related topic)
  - Documents about "cybersecurity" (related topic)
  - Documents about "protection" (related topic)
```

---

## ⚙️ Configuration

### Minimum Requirements
- **Data**: 5-10 documents in your table
- **Searches**: 3-5 different searches to train
- **Topics**: Configured as 5 topics (can be changed in `LDATopicModelingService.NUM_TOPICS`)

### Check Model Status
```bash
curl -X GET "http://localhost:8080/api/topics/model/info"
```

**Before training:**
```json
{"status": "No model trained yet"}
```

**After training:**
```json
{
  "numDocuments": 15,
  "vocabularySize": 200,
  "topics": {
    "0": ["security", "defence", "cyber", "attack", "protection"],
    "1": ["data", "privacy", "encryption", "secure", "information"],
    ...
  }
}
```

---

## 🎯 Your Use Case

### What You Have
- Table: `actualities`
- Data: Documents about security, defence, cybersecurity, etc.

### What You Want
```
Search: "defence"
Get: All defence + security + cybersecurity documents
```

### Solution
```bash
# Use topic-aware search endpoint
POST /api/topics/search

# With body:
{
  "query": "defence",
  "tableName": "actualities",
  "searchFields": ["title", "content"]
}
```

---

## 📝 Important Notes

### Model Needs Training
- First search: No model yet, returns only direct matches
- After 3-5 searches: Model starts learning
- After 10+ searches: Model works well

### Automatic Training
- Model trains automatically during searches
- No manual intervention needed (but you can trigger with `/api/topics/model/retrain`)
- More diverse searches = better model

### Best Practices
1. Do searches on different related terms (security, defence, cyber, protection)
2. Let the model see at least 10-15 documents
3. Use topic-aware search for discovery, standard search for exact matches

---

## ✅ Success Checklist

- [ ] Application running on port 8080
- [ ] Database has data in `actualities` table
- [ ] Performed 5+ different searches
- [ ] Model status shows topics and vocabulary
- [ ] Searching "defence" returns security-related documents
- [ ] Searching "security" returns defence-related documents

---

## 🚀 Quick Commands

```bash
# 1. Check if app is running
curl -X GET "http://localhost:8080/actuator/health"

# 2. Run demo script
./demo_topic_search.sh

# 3. Check model
curl -X GET "http://localhost:8080/api/topics/model/info" | python3 -m json.tool

# 4. Test your use case
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}' \
  | python3 -m json.tool
```

---

## 🎉 That's It!

**Now when you search for "defence", you'll get all related security documents!**

The key is:
1. ✅ Use `/api/topics/search` (not `/api/smart-search`)
2. ✅ Do multiple searches to train the model
3. ✅ Let the LDA algorithm discover topic relationships

**Happy searching! 🚀**
