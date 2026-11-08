#!/bin/bash

echo "========================================"
echo "开始执行微服务与依赖健康检查..."
echo "========================================"

FAIL=0

# 1. 检查 Nacos 控制台 (使用 18080 新地址)
NACOS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/)
# Nacos 3.x 有时会返回 200 或 302 重定向
if [ "$NACOS_STATUS" -eq 200 ] || [ "$NACOS_STATUS" -eq 302 ]; then
    echo "✅ Nacos 控制台正常 (HTTP $NACOS_STATUS)"
else
    echo "❌ Nacos 控制台异常 (HTTP $NACOS_STATUS)"
    FAIL=1
fi

# 2. 检查各个微服务的 Actuator 健康接口
check_health() {
    SERVICE_NAME=$1
    URL=$2
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$URL")

    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ $SERVICE_NAME 运行正常 (HTTP 200)"
    else
        echo "❌ $SERVICE_NAME 异常 (HTTP $HTTP_STATUS)"
        FAIL=1
    fi
}

check_health "user-service" "http://localhost:8081/actuator/health"
check_health "vehicle-service" "http://localhost:8082/actuator/health"
check_health "alert-service" "http://localhost:8083/actuator/health"
check_health "driving-behavior-service" "http://localhost:8084/actuator/health"
check_health "carbon-service" "http://localhost:8085/actuator/health"

echo "========================================"
# 3. 退出状态判定
if [ "$FAIL" -ne 0 ]; then
    echo "🚨 检查失败：存在异常服务或依赖不可用，退出码 1"
    exit 1
else
    echo "🎉 所有检查通过，退出码 0"
    exit 0
fi