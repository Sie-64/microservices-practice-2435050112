#!/usr/bin/env bash
set -u

echo "开始执行微服务健康检查..."

FAILED=0
HOST="${SMOKE_TEST_HOST:-127.0.0.1}"

check_health() {
    SERVICE_NAME=$1
    URL=$2
    HTTP_STATUS=$(curl -sS -o /dev/null -w "%{http_code}" "$URL" || true)
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ $SERVICE_NAME 运行正常 (HTTP 200)"
    else
        echo "❌ $SERVICE_NAME 异常 (HTTP $HTTP_STATUS)"
        FAILED=1
    fi
}

check_health "user-service" "http://${HOST}:8081/actuator/health"
check_health "vehicle-service" "http://${HOST}:8082/actuator/health"
check_health "alert-service" "http://${HOST}:8083/actuator/health"
check_health "driving-behavior-service" "http://${HOST}:8084/actuator/health"
check_health "carbon-service" "http://${HOST}:8085/actuator/health"
check_health "nacos-console" "http://${HOST}:18080/next/"

echo "健康检查执行完毕。"
exit "$FAILED"
