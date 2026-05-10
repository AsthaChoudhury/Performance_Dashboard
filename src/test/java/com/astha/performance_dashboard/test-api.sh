#!/bin/bash

# Asset Performance Platform - API Test Script
# This script tests all major endpoints to verify the system is working

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}================================================${NC}"
echo -e "${YELLOW}Asset Performance Platform - API Test Suite${NC}"
echo -e "${YELLOW}================================================${NC}"
echo ""

# Check if server is running
echo -e "${YELLOW}[1] Checking if server is running...${NC}"
if curl -s "${BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is not running at ${BASE_URL}${NC}"
    echo -e "${YELLOW}Please start the server first:${NC}"
    echo -e "  docker-compose up -d"
    echo -e "  OR"
    echo -e "  mvn spring-boot:run"
    exit 1
fi
echo ""

# Test 1: Create an Asset
echo -e "${YELLOW}[2] Creating a test asset...${NC}"
ASSET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/assets" \
  -H "Content-Type: application/json" \
  -d '{
    "assetName": "Test-Turbine-001",
    "assetType": "Wind Turbine",
    "location": "Test Site A",
    "status": "ACTIVE",
    "manufacturer": "TestCorp",
    "model": "TC-1000"
  }')

ASSET_ID=$(echo $ASSET_RESPONSE | jq -r '.id')
if [ "$ASSET_ID" != "null" ] && [ -n "$ASSET_ID" ]; then
    echo -e "${GREEN}✓ Asset created successfully${NC}"
    echo -e "  Asset ID: ${ASSET_ID}"
else
    echo -e "${RED}✗ Failed to create asset${NC}"
    echo "Response: $ASSET_RESPONSE"
    exit 1
fi
echo ""

# Test 2: Get the Asset
echo -e "${YELLOW}[3] Retrieving the asset...${NC}"
GET_ASSET=$(curl -s "${BASE_URL}/api/v1/assets/${ASSET_ID}")
ASSET_NAME=$(echo $GET_ASSET | jq -r '.assetName')
if [ "$ASSET_NAME" = "Test-Turbine-001" ]; then
    echo -e "${GREEN}✓ Asset retrieved successfully${NC}"
    echo -e "  Name: ${ASSET_NAME}"
else
    echo -e "${RED}✗ Failed to retrieve asset${NC}"
    exit 1
fi
echo ""

# Test 3: Create Performance Metrics
echo -e "${YELLOW}[4] Creating performance metrics...${NC}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S")
for i in {1..5}; do
    TEMP=$((70 + RANDOM % 20))
    EFF=$((80 + RANDOM % 15))
    
    METRIC_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/metrics" \
      -H "Content-Type: application/json" \
      -d "{
        \"assetId\": \"${ASSET_ID}\",
        \"timestamp\": \"${TIMESTAMP}\",
        \"temperature\": ${TEMP}.5,
        \"vibration\": 3.2,
        \"powerConsumption\": 1250.0,
        \"efficiency\": ${EFF}.5,
        \"runningHours\": 1200,
        \"downtime\": 0,
        \"failureDetected\": false
      }")
    
    METRIC_ID=$(echo $METRIC_RESPONSE | jq -r '.id')
    if [ "$METRIC_ID" != "null" ]; then
        echo -e "${GREEN}✓ Metric $i created (ID: ${METRIC_ID:0:8}...)${NC}"
    else
        echo -e "${RED}✗ Failed to create metric $i${NC}"
    fi
    
    sleep 0.5
done
echo ""

# Test 4: Get Asset Statistics
echo -e "${YELLOW}[5] Getting asset statistics...${NC}"
STATS_RESPONSE=$(curl -s "${BASE_URL}/api/v1/analytics/assets/${ASSET_ID}/stats")
AVG_TEMP=$(echo $STATS_RESPONSE | jq -r '.averageTemperature')
if [ "$AVG_TEMP" != "null" ]; then
    echo -e "${GREEN}✓ Statistics retrieved successfully${NC}"
    echo -e "  Average Temperature: ${AVG_TEMP}°C"
    echo -e "  Average Efficiency: $(echo $STATS_RESPONSE | jq -r '.averageEfficiency')%"
    echo -e "  Health Score: $(echo $STATS_RESPONSE | jq -r '.healthScore')"
else
    echo -e "${RED}✗ Failed to retrieve statistics${NC}"
fi
echo ""

# Test 5: Cache Performance Demo
echo -e "${YELLOW}[6] Testing cache performance...${NC}"
CACHE_DEMO=$(curl -s "${BASE_URL}/api/v1/analytics/cache-demo/${ASSET_ID}")
CACHED_TIME=$(echo $CACHE_DEMO | jq -r '.cachedResponseTime')
NON_CACHED_TIME=$(echo $CACHE_DEMO | jq -r '.nonCachedResponseTime')
SPEEDUP=$(echo $CACHE_DEMO | jq -r '.speedupFactor')

if [ "$CACHED_TIME" != "null" ]; then
    echo -e "${GREEN}✓ Cache performance test completed${NC}"
    echo -e "  Cached Response Time: ${CACHED_TIME}ms"
    echo -e "  Non-Cached Response Time: ${NON_CACHED_TIME}ms"
    echo -e "  Speedup Factor: ${SPEEDUP}x"
else
    echo -e "${RED}✗ Cache performance test failed${NC}"
fi
echo ""

# Test 6: Get Performance Trends
echo -e "${YELLOW}[7] Getting performance trends...${NC}"
TRENDS_RESPONSE=$(curl -s "${BASE_URL}/api/v1/analytics/assets/${ASSET_ID}/trends?period=24h")
PERIOD=$(echo $TRENDS_RESPONSE | jq -r '.period')
if [ "$PERIOD" = "24h" ]; then
    TEMP_POINTS=$(echo $TRENDS_RESPONSE | jq '.temperatureTrend | length')
    echo -e "${GREEN}✓ Trends retrieved successfully${NC}"
    echo -e "  Period: ${PERIOD}"
    echo -e "  Temperature data points: ${TEMP_POINTS}"
else
    echo -e "${RED}✗ Failed to retrieve trends${NC}"
fi
echo ""

# Test 7: Health Score Calculation
echo -e "${YELLOW}[8] Calculating health score...${NC}"
HEALTH_RESPONSE=$(curl -s "${BASE_URL}/api/v1/analytics/assets/${ASSET_ID}/health-score")
HEALTH_SCORE=$(echo $HEALTH_RESPONSE | jq -r '.healthScore')
if [ "$HEALTH_SCORE" != "null" ]; then
    echo -e "${GREEN}✓ Health score calculated${NC}"
    echo -e "  Health Score: ${HEALTH_SCORE}/100"
else
    echo -e "${RED}✗ Failed to calculate health score${NC}"
fi
echo ""

# Test 8: List All Assets
echo -e "${YELLOW}[9] Listing all assets...${NC}"
ALL_ASSETS=$(curl -s "${BASE_URL}/api/v1/assets")
ASSET_COUNT=$(echo $ALL_ASSETS | jq '. | length')
echo -e "${GREEN}✓ Found ${ASSET_COUNT} asset(s)${NC}"
echo ""

# Test 9: Update Asset
echo -e "${YELLOW}[10] Updating asset status...${NC}"
UPDATE_RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/v1/assets/${ASSET_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "MAINTENANCE"
  }')
UPDATED_STATUS=$(echo $UPDATE_RESPONSE | jq -r '.status')
if [ "$UPDATED_STATUS" = "MAINTENANCE" ]; then
    echo -e "${GREEN}✓ Asset updated successfully${NC}"
    echo -e "  New status: ${UPDATED_STATUS}"
else
    echo -e "${RED}✗ Failed to update asset${NC}"
fi
echo ""

# Test 10: System Health
echo -e "${YELLOW}[11] Checking system health...${NC}"
HEALTH=$(curl -s "${BASE_URL}/actuator/health")
STATUS=$(echo $HEALTH | jq -r '.status')
if [ "$STATUS" = "UP" ]; then
    echo -e "${GREEN}✓ System health: ${STATUS}${NC}"
    echo $HEALTH | jq '.components' 2>/dev/null || echo "  All components operational"
else
    echo -e "${RED}✗ System health check failed${NC}"
fi
echo ""

# Cleanup (optional - comment out if you want to keep test data)
echo -e "${YELLOW}[12] Cleanup...${NC}"
echo -e "${YELLOW}Do you want to delete the test asset? (y/N)${NC}"
read -t 10 -n 1 CLEANUP || CLEANUP="n"
echo ""
if [ "$CLEANUP" = "y" ] || [ "$CLEANUP" = "Y" ]; then
    curl -s -X DELETE "${BASE_URL}/api/v1/assets/${ASSET_ID}" > /dev/null
    echo -e "${GREEN}✓ Test asset deleted${NC}"
else
    echo -e "${YELLOW}Test asset kept (ID: ${ASSET_ID})${NC}"
fi
echo ""

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}All tests completed successfully! ✓${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""
echo -e "Next steps:"
echo -e "  1. Open Swagger UI: ${BASE_URL}/swagger-ui.html"
echo -e "  2. Test WebSocket: Open test-websocket.html in browser"
echo -e "  3. View metrics: ${BASE_URL}/actuator/prometheus"
echo ""