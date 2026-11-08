#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

HOST="${SMOKE_TEST_HOST:-127.0.0.1}"
VEHICLE_URL="http://${HOST}:8082/api/vehicles"
ALERT_URL="http://${HOST}:8083/api/alerts"

wait_for_url() {
    local url="$1"
    local expected="$2"
    local attempts="${3:-60}"
    local status=""
    for _ in $(seq 1 "${attempts}"); do
        status="$(curl -sS -o /dev/null -w '%{http_code}' "${url}" || true)"
        if [[ "${status}" == "${expected}" ]]; then
            return 0
        fi
        sleep 2
    done
    echo "等待 ${url} 返回 ${expected} 失败，最后状态：${status}"
    return 1
}

POST_BODY_FILE="$(mktemp)"

post_alert() {
    local vehicle_id="$1"
    local status
    status="$(curl -sS -o "${POST_BODY_FILE}" -w '%{http_code}' \
        -X POST "${ALERT_URL}" \
        -H "Content-Type: application/json" \
        -d "{\"vehicleId\":${vehicle_id},\"alertType\":\"discovery-check\",\"level\":1}" \
        || true)"
    printf '%s' "${status}"
}

response_body() {
    cat "${POST_BODY_FILE}"
}

restore_vehicle() {
    docker compose start vehicle-service >/dev/null 2>&1 || true
}

cleanup() {
    restore_vehicle
    rm -f "${POST_BODY_FILE}"
}
trap cleanup EXIT

bash scripts/smoke-test.sh
wait_for_url "http://${HOST}:8082/actuator/health" 200
wait_for_url "http://${HOST}:8083/actuator/health" 200

PLATE="SMOKE-$(date +%s)"
VEHICLE_RESPONSE="$(curl -fsS -X POST "${VEHICLE_URL}" \
    -H "Content-Type: application/json" \
    -d "{\"plateNumber\":\"${PLATE}\",\"brand\":\"DiscoveryCheck\"}")"
VEHICLE_ID="$(python3 -c 'import json,sys; print(json.loads(sys.argv[1])["id"])' \
    "${VEHICLE_RESPONSE}")"

NORMAL_STATUS="$(post_alert "${VEHICLE_ID}")"
if [[ "${NORMAL_STATUS}" != "200" ]]; then
    echo "正常调用失败，HTTP ${NORMAL_STATUS}：$(response_body)"
    exit 1
fi
echo "✅ 正常调用：alert-service 通过 Nacos 找到 vehicle-service"

docker compose stop vehicle-service >/dev/null
echo "已停止 vehicle-service，等待实例下线"

OFFLINE_STATUS=""
for _ in $(seq 1 45); do
    OFFLINE_STATUS="$(post_alert "${VEHICLE_ID}")"
    if [[ "${OFFLINE_STATUS}" == "503" ]]; then
        break
    fi
    sleep 2
done

if [[ "${OFFLINE_STATUS}" != "503" ]]; then
    echo "服务下线后预期 HTTP 503，实际：${OFFLINE_STATUS}，响应：$(response_body)"
    exit 1
fi
echo "✅ 服务下线：空实例被识别，业务调用返回 HTTP 503"

docker compose start vehicle-service >/dev/null
wait_for_url "http://${HOST}:8082/actuator/health" 200 90

RECOVERED_STATUS=""
for _ in $(seq 1 45); do
    RECOVERED_STATUS="$(post_alert "${VEHICLE_ID}")"
    if [[ "${RECOVERED_STATUS}" == "200" ]]; then
        break
    fi
    sleep 2
done

if [[ "${RECOVERED_STATUS}" != "200" ]]; then
    echo "服务恢复后调用仍失败，HTTP ${RECOVERED_STATUS}：$(response_body)"
    exit 1
fi
echo "✅ 服务恢复：实例重新注册后业务调用恢复为 HTTP 200"
