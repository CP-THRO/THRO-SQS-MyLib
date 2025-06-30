#!/bin/sh

curl -X POST http://backend:8080/api/v1/auth/add-user   -H "Origin: http://frontend:80"   -H "Content-Type: application/json"   -d '{"username":"testuser","password":"testpassword"}'
TOKEN=$(curl -s -X POST http://backend:8080/api/v1/auth/authenticate -H "Origin: http://frontend:80" -H "Content-Type: application/json" -d '{"username":"testuser","password":"testpassword"}')

echo "Runing authenticated scan"
python3 zap-api-scan.py -t http://backend:8080/v3/api-docs -f openapi -z "auth.header=Origin:\ http://frontend:80" -z "auth.header=Authorization:\ Bearer\ $TOKEN" -r api-report-auth.html -J api-report-auth.json -d
