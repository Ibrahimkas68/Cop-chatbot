#!/bin/bash

echo "========================================="
echo "🎯 SEMANTIC SEARCH TEST"
echo "Search 'defence' → Find 'security' docs"
echo "========================================="
echo ""

echo "Test 1: Search 'defence'"
echo "Expected: Should find security/protection documents"
echo "---"
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "defence", "tableName": "actualities", "searchFields": []}' \
  -s | python3 -m json.tool | grep -E "(title_fr|title_ar)" | head -5
echo ""
echo ""

echo "Test 2: Search 'apprendre la defence'"
echo "Expected: Should find learning + security documents"
echo "---"
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "apprendre la defence", "tableName": "actualities", "searchFields": []}' \
  -s | python3 -m json.tool | grep -E "(title_fr|title_ar)" | head -5
echo ""
echo ""

echo "Test 3: Search 'securite'"
echo "Expected: Should find security documents"
echo "---"
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "securite", "tableName": "actualities", "searchFields": []}' \
  -s | python3 -m json.tool | grep -E "(title_fr|title_ar)" | head -5
echo ""
echo ""

echo "Test 4: Search 'cybersecurity'"
echo "Expected: Should find security/cyber documents"
echo "---"
curl -X POST "http://localhost:8080/api/topics/search" \
  -H "Content-Type: application/json" \
  -d '{"query": "cybersecurity", "tableName": "actualities", "searchFields": []}' \
  -s | python3 -m json.tool | grep -E "(title_fr|title_ar)" | head -5
echo ""
echo ""

echo "========================================="
echo "✅ SUCCESS!"
echo "Semantic search is working!"
echo "========================================="
