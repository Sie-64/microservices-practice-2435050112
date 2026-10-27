#!/bin/bash
echo "开始执行微服务健康检查..."

check_health() {
    SERVICE_NAME=$1
    URL=$2
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$URL")
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ $SERVICE_NAME 运行正常 (HTTP 200)"
    else
        echo "❌ $SERVICE_NAME 异常 (HTTP $HTTP_STATUS)"
    fi
}

check_health "user-service" "http://localhost:8081/actuator/health"
check_health "vehicle-service" "http://localhost:8082/actuator/health"
check_health "alert-service" "http://localhost:8083/actuator/health"

echo "健康检查执行完毕。"
