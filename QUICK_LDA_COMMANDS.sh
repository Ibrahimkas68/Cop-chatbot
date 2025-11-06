#!/bin/bash

# 🎯 Quick LDA Commands Reference
# Run these commands to use the real LDA topic modeling

echo "=== Real LDA Topic Modeling Quick Commands ==="
echo ""

# 1. Start the application (if not running)
echo "1. START APPLICATION:"
echo "   ./mvnw spring-boot:run"
echo ""

# 2. Train the LDA model
echo "2. TRAIN LDA MODEL FROM DATABASE:"
echo "   curl -X POST 'http://localhost:8080/api/topics/model/train?tableName=actualities'"
echo "   # Note: This is async - training happens in background"
echo ""

# 3. Check training progress
echo "3. CHECK MODEL STATUS (shows discovered topics):"
echo "   curl -X GET 'http://localhost:8080/api/topics/model/status' | jq ."
echo ""

# 4. Perform topic-aware search
echo "4. TOPIC-AWARE SEARCH (finds related documents):"
echo "   curl -X POST 'http://localhost:8080/api/topics/search' \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"query\": \"defence\", \"tableName\": \"actualities\", \"searchFields\": [\"title\", \"content\"]}' | jq ."
echo ""

# 5. Clear the model
echo "5. CLEAR MODEL (for retraining):"
echo "   curl -X DELETE 'http://localhost:8080/api/topics/model'"
echo ""

echo "=== COMPLETE WORKFLOW ==="
echo ""
echo "Step 1: Train the model"
echo "  curl -X POST 'http://localhost:8080/api/topics/model/train?tableName=actualities'"
echo ""
echo "Step 2: Wait 30-60 seconds for training..."
echo ""
echo "Step 3: Check if training completed"
echo "  curl -X GET 'http://localhost:8080/api/topics/model/status' | jq '.trained'"
echo ""
echo "Step 4: Search for related documents"
echo "  curl -X POST 'http://localhost:8080/api/topics/search' \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"query\": \"security\", \"tableName\": \"actualities\"}' | jq '.results | length'"
echo ""

echo "=== WHAT'S NEW ==="
echo "✅ Real LDA topic modeling (not hardcoded)"
echo "✅ Discovers topics from YOUR database"
echo "✅ Learns automatically from content"
echo "✅ Better semantic search"
echo ""

echo "=== MODEL SETTINGS ==="
echo "Number of topics: 10"
echo "Training iterations: 1000"
echo "Min documents required: 10+"
echo "Recommended documents: 20+"
echo ""

echo "Edit src/main/java/.../LDATopicModelService.java to change these settings"
