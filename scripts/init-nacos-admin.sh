#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "缺少 ${ENV_FILE}，请先执行 bash scripts/prepare-nacos-env.sh"
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

CONSOLE_URL="${NACOS_CONSOLE_URL:-http://localhost:18080}"
USERNAME="${NACOS_USERNAME:-nacos}"
PASSWORD="${NACOS_PASSWORD:-}"

wait_for_console() {
    for _ in $(seq 1 60); do
        if curl -fsS "${CONSOLE_URL}/next/" >/dev/null 2>&1; then
            return 0
        fi
        sleep 2
    done
    echo "Nacos 控制台在 120 秒内未就绪：${CONSOLE_URL}"
    return 1
}

login() {
    if [[ -z "${PASSWORD}" ]]; then
        return 1
    fi
    local response
    response="$(curl -fsS -X POST "${CONSOLE_URL}/v3/auth/user/login" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        --data-urlencode "username=${USERNAME}" \
        --data-urlencode "password=${PASSWORD}")" || return 1
    python3 -c '
import json
import sys

payload = json.loads(sys.argv[1])
if not payload.get("accessToken"):
    raise SystemExit("登录响应缺少 accessToken")
' "${response}" >/dev/null 2>&1
}

update_env_credentials() {
    python3 - "${ENV_FILE}" "${USERNAME}" "${PASSWORD}" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
username = sys.argv[2]
password = sys.argv[3]
updates = {"NACOS_USERNAME": username, "NACOS_PASSWORD": password}
lines = path.read_text(encoding="utf-8").splitlines()
seen = set()
result = []

for line in lines:
    if "=" in line and not line.lstrip().startswith("#"):
        key = line.split("=", 1)[0].strip()
        if key in updates:
            value = updates[key].replace("'", "'\"'\"'")
            result.append(f"{key}='{value}'")
            seen.add(key)
            continue
    result.append(line)

for key, value in updates.items():
    if key not in seen:
        value = value.replace("'", "'\"'\"'")
        result.append(f"{key}='{value}'")

path.write_text("\n".join(result).rstrip() + "\n", encoding="utf-8")
path.chmod(0o600)
PY
}

wait_for_console

if login; then
    echo "Nacos 管理员已初始化，当前 .env 凭据登录成功。"
    exit 0
fi

RESPONSE="$(curl -sS -X POST "${CONSOLE_URL}/v3/auth/user/admin" \
    -H "Content-Type: application/json" \
    -d '{}')"

PARSED="$(python3 -c '
import json
import sys

payload = json.loads(sys.argv[1])
if payload.get("code") != 0:
    raise SystemExit(payload.get("message", "管理员初始化失败"))
data = payload.get("data") or {}
if not data.get("username") or not data.get("password"):
    raise SystemExit("初始化响应缺少管理员凭据")
print(data["username"])
print(data["password"])
' "${RESPONSE}")"

USERNAME="$(printf '%s\n' "${PARSED}" | sed -n '1p')"
PASSWORD="$(printf '%s\n' "${PARSED}" | sed -n '2p')"
update_env_credentials

if ! login; then
    echo "管理员密码已写入 .env，但登录验证失败。"
    exit 1
fi

echo "Nacos 管理员 ${USERNAME} 初始化成功，密码已写入本地 .env。"
