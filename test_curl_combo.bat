@echo off
echo Testing Temp 1.0 AND Stream False
curl -X POST "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer S14P02AG04-a85b76ca-d893-420e-b425-72c2d9d8fadb" ^
  -d "{\"model\": \"gpt-5\", \"messages\": [{\"role\": \"user\", \"content\": \"Hello\"}], \"temperature\": 1.0, \"stream\": false}"
