@echo off
curl -X POST "http://localhost:8080/api/v1/llm" ^
  -H "accept: */*" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHJpbmciLCJhdXRoIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzY2NDgyMzYwLCJleHAiOjE3NjY1Njg3NTl9.jhFdvFNNwDl4zC2XknXoExlQetEyQ1rHdiQ4lpGG6EA" ^
  -H "Content-Type: application/json" ^
  -d "{\"llmRequestId\": 0, \"userId\": 0, \"aptSeq\": \"string\", \"type\": \"string\", \"prompt\": \"Test prompt\", \"response\": \"string\", \"conditionJson\": \"string\", \"createdAt\": \"2025-12-23T09:33:02.930Z\"}"
