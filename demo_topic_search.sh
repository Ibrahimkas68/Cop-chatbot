#!/bin/bash

echo "========================================="
echo "DEMO: Topic-Aware Search"
echo "Goal: Search 'defence' → Get 'security' docs"
echo "========================================="
echo ""

echo "Step 1: Train the model by doing multiple searches..."
echo ""

echo "→ Searching for 'security'..."
curl -s -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "security", "tableName": "actualities", "searchFields": ["title", "content"]}' > /dev/null
echo "✓ Done"

echo "→ Searching for 'defence'..."
curl -s -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}' > /dev/null
echo "✓ Done"

echo "→ Searching for 'cybersecurity'..."
curl -s -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "cybersecurity", "tableName": "actualities", "searchFields": ["title", "content"]}' > /dev/null
echo "✓ Done"

echo "→ Searching for 'protection'..."
curl -s -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "protection", "tableName": "actualities", "searchFields": ["title", "content"]}' > /dev/null
echo "✓ Done"

echo ""
echo "Step 2: Check model status..."
echo ""
curl -s -X GET "http://localhost:8080/api/topics/model/info" | python3 -m json.tool
echo ""

echo "========================================="
echo "Step 3: NOW TEST - Search 'defence'"
echo "Should return security-related documents!"
echo "========================================="
echo ""
curl -s -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": ["title", "content"]}' | python3 -m json.tool

echo ""
echo "========================================="
echo "✅ If you see multiple documents above,"
echo "including ones about 'security', it works!"
echo "========================================="
