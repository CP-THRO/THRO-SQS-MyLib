#!/bin/sh

curl -X POST http://backend:8080/api/v1/auth/add-user   -H "Origin: http://frontend:80"   -H "Content-Type: application/json"   -d '{"username":"testuser","password":"testpassword"}'
TOKEN=$(curl -s -X POST http://backend:8080/api/v1/auth/authenticate -H "Origin: http://frontend:80" -H "Content-Type: application/json" -d '{"username":"testuser","password":"testpassword"}')

echo "Runing authenticated scan"
python3 zap-full-scan.py -t http://frontend:80 -z "auth.header=Authorization:\ Bearer\ $TOKEN" -r full-report-authenticated.html -J full-report-authenticated.json -d
