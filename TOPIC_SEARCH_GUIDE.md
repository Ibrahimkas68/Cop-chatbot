# 🎯 How to Make "Defence" Return "Security" Documents

## Your Goal
When you search for **"defence"**, you want it to also return documents about **"security"**, **"cybersecurity"**, **"protection"**, etc. because they are **related topics**.

## How It Works

### 1. **Standard Search** (`/api/smart-search`)
- Only returns **exact matches**
- Search "defence" → Only gets documents with "defence" in them
- ❌ Won't find related "security" documents

### 2. **Topic-Aware Search** (`/api/topics/search`) ✅
- Returns **exact matches** PLUS **related documents**
- Search "defence" → Gets "defence" documents + "security" + "cybersecurity" + "protection"
- ✅ Uses LDA model to find topically similar documents

---

## 🚀 Step-by-Step Setup

### Step 1: Make Sure You Have Data

Your `actualities` table needs documents about both "defence" and "security":

```sql
-- Check if you have data
SELECT title FROM actualities WHERE 
  title ILIKE '%defence%' OR 
  title ILIKE '%security%' OR
  title ILIKE '%sécurité%' OR
  title ILIKE '%défense%';
```

### Step 2: Perform Multiple Searches to Build the Model

The model learns by seeing multiple documents. Do several searches:

```bash
# Search 1: Defence
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "defence",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'

# Search 2: Security  
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "security",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'

# Search 3: Cybersecurity
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "cybersecurity",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'

# Search 4: Protection
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "protection",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }'
```

**What happens:** Each search adds documents to the cache and trains the model!

### Step 3: Check Model Status

```bash
curl -X GET "http://localhost:8080/api/topics/model/info"
```

**Before training:**
```json
{"status": "No model trained yet"}
```

**After training (what you want to see):**
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

### Step 4: Now Test Your Use Case!

```bash
# Search for "defence" - should return security-related documents too!
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "defence",
    "tableName": "actualities",
    "searchFields": ["title", "content"]
  }' | python3 -m json.tool
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
    "title": "Cybersecurity Best Practices",  // ← Related document!
    "score": 0.82
  },
  {
    "id": "8",
    "title": "Network Security",  // ← Related document!
    "score": 0.78
  }
]
```

---

## 🔍 Understanding the Magic

### How LDA Finds Related Documents

1. **Topic Discovery**: LDA analyzes all documents and discovers hidden topics
   - Topic 0: [security, defence, cyber, attack, threat]
   - Topic 1: [data, privacy, encryption, protection]
   - Topic 2: [network, firewall, infrastructure]

2. **Document Representation**: Each document gets a "topic distribution"
   - Document about "defence": 80% Topic 0, 15% Topic 1, 5% Topic 2
   - Document about "security": 75% Topic 0, 20% Topic 1, 5% Topic 2
   - Document about "cloud": 10% Topic 0, 15% Topic 1, 75% Topic 2

3. **Similarity Matching**: When you search "defence":
   - Finds direct matches first
   - Then finds documents with similar topic distributions
   - Returns both together!

---

## 📊 Example Scenario

### Your Database Has:
```
1. "Apprendre la défense cybernétique"
2. "Formation en sécurité informatique"
3. "Stratégies de défense contre les cyberattaques"
4. "Cloud Security Best Practices"
5. "Network Security Fundamentals"
```

### You Search: "defence"

**Standard Search** (`/api/smart-search`):
```json
[
  {"id": "1", "title": "Apprendre la défense cybernétique"},
  {"id": "3", "title": "Stratégies de défense contre les cyberattaques"}
]
```

**Topic-Aware Search** (`/api/topics/search`):
```json
[
  {"id": "1", "title": "Apprendre la défense cybernétique", "score": 0.95},
  {"id": "3", "title": "Stratégies de défense contre les cyberattaques", "score": 0.92},
  {"id": "2", "title": "Formation en sécurité informatique", "score": 0.85},  // ← Added!
  {"id": "5", "title": "Network Security Fundamentals", "score": 0.78}  // ← Added!
]
```

---

## ⚠️ Important Notes

### Model Needs Training Data
- Minimum: 5-10 documents
- Recommended: 20+ documents
- More documents = better topic discovery

### Model Learns from Searches
- Each search adds documents to the cache
- Model retrains automatically when cache updates
- More diverse searches = better model

### First Few Searches
- First search: Model not trained yet, returns only direct matches
- After 3-5 searches: Model starts learning
- After 10+ searches: Model works well

---

## 🎯 Quick Test Commands

```bash
# 1. Start the application
./mvnw spring-boot:run

# 2. Do several searches to train the model
for term in "defence" "security" "cybersecurity" "protection" "attack"; do
  curl -X POST "http://localhost:8080/api/topics/search" \
    -H "Content-Type: application/json" \
    -d "{\"query\": \"$term\", \"tableName\": \"actualities\", \"searchFields\": [\"title\", \"content\"]}"
  echo ""
done

# 3. Check model status
curl -X GET "http://localhost:8080/api/topics/model/info" | python3 -m json.tool

# 4. Test your use case
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}' \
  | python3 -m json.tool
```

---

## ✅ Success Checklist

- [ ] Database has documents about defence, security, cybersecurity
- [ ] Performed 5+ different searches to train the model
- [ ] Model info shows topics with related terms
- [ ] Searching "defence" returns security-related documents
- [ ] Searching "security" returns defence-related documents

**That's it! Now "defence" will find "security" documents! 🎉**
