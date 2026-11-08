#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"
EXAMPLE_FILE="${ROOT_DIR}/.env.example"

if [[ ! -f "${ENV_FILE}" ]]; then
    cp "${EXAMPLE_FILE}" "${ENV_FILE}"
    echo "已从 .env.example 创建本地 .env"
fi

python3 - "${ENV_FILE}" <<'PY'
import base64
import secrets
import shlex
import sys
from pathlib import Path

path = Path(sys.argv[1])
lines = path.read_text(encoding="utf-8").splitlines()
values = {}


def parse_assignment(line):
    if not line.lstrip() or line.lstrip().startswith("#") or "=" not in line:
        return None
    try:
        parts = shlex.split(line, comments=False, posix=True)
    except ValueError:
        return None
    if len(parts) != 1 or "=" not in parts[0]:
        return None
    key, value = parts[0].split("=", 1)
    return key.strip(), value


for line in lines:
    assignment = parse_assignment(line)
    if assignment is not None:
        key, value = assignment
        values[key] = value

generated = []


def ensure(key, default, placeholders=()):
    current = values.get(key, "")
    if not current or current in placeholders:
        values[key] = default
        generated.append(key)


ensure(
    "MYSQL_ROOT_PASSWORD",
    secrets.token_urlsafe(24),
    {"change_me", "your_secure_password_here"},
)
ensure("MYSQL_NACOS_USER", "nacos")
ensure(
    "MYSQL_NACOS_PASSWORD",
    secrets.token_urlsafe(24),
    {"change_me", "change_me_nacos"},
)
ensure("NACOS_USERNAME", "nacos")
ensure("NACOS_PASSWORD", "")
ensure("NACOS_GROUP", "COURSE_GROUP")
ensure(
    "NACOS_AUTH_TOKEN",
    base64.b64encode(secrets.token_bytes(32)).decode("ascii"),
    {"change_me", "replace_with_base64_secret"},
)
ensure(
    "NACOS_AUTH_IDENTITY_KEY",
    "nacos-" + secrets.token_hex(8),
    {"change_me", "replace_me"},
)
ensure(
    "NACOS_AUTH_IDENTITY_VALUE",
    secrets.token_hex(32),
    {"change_me", "replace_me"},
)
ensure("NACOS_SERVER_ADDR", "nacos:8848")
if values.get("NACOS_SERVER_ADDR") == "localhost:8848":
    values["NACOS_SERVER_ADDR"] = "nacos:8848"
    generated.append("NACOS_SERVER_ADDR")
ensure("NACOS_CONSOLE_URL", "http://localhost:18080")
ensure("CARBON_GASOLINE_FACTOR", "2.31")
ensure("CARBON_GRID_FACTOR", "0.581")

kept = []
existing_keys = set()
for line in lines:
    assignment = parse_assignment(line)
    if assignment is not None:
        key = assignment[0]
        existing_keys.add(key)
        value = shlex.quote(values[key])
        kept.append(f"{key}={value}")
    else:
        kept.append(line)

for key in values:
    if key not in existing_keys:
        value = shlex.quote(values[key])
        kept.append(f"{key}={value}")

path.write_text("\n".join(kept).rstrip() + "\n", encoding="utf-8")
print("已准备本地 Nacos/MySQL 环境变量：" + ", ".join(sorted(generated)))
PY

chmod 600 "${ENV_FILE}"
echo "本地密钥仅写入 ${ENV_FILE}，不要提交该文件。"
