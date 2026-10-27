# 智驾伴侣 - 微服务拆分设计

## 1. 服务拆分映射
- `user-service`（用户服务）：负责车主账号、套餐等信息（对应库：user_db，表：users）
- `vehicle-service`（车辆服务）：负责车辆档案、车型、车牌号（对应库：vehicle_db，表：vehicles）
- `alert-service`（告警服务）：负责车辆告警信息、级别（对应库：alert_db，表：alerts）

## 2. 服务职责与端口
| 服务名 | 端口 | 职责 | 数据库表 | 数据归属 |
|--------|------|------|----------|----------|
| user-service | 8081 | 管理车主信息 | users | 仅该服务可读写 |
| vehicle-service | 8082 | 管理车辆档案 | vehicles | 仅该服务可读写 |
| alert-service | 8083 | 管理车辆告警 | alerts | 仅该服务可读写 |

## 3. 服务调用关系
- 前端/网关请求 `alert-service` 创建告警。
- `alert-service` 收到请求后，通过 `RestTemplate` 调用 `vehicle-service` 的 `GET /api/vehicles/{id}` 接口，校验车辆是否存在。
- 车辆存在则写入告警库；车辆不存在则直接返回 400 错误，拒绝写入。

## 4. Mermaid 架构图

```mermaid
graph TD
    Client[客户端] --> |HTTP| Alert[alert-service :8083]
    Client --> |HTTP| Vehicle[vehicle-service :8082]
    Alert --> |RestTemplate 查询车辆| Vehicle
    Alert -.-> |读写| DB_Alert[(alert_db)]
    Vehicle -.-> |读写| DB_Vehicle[(vehicle_db)]
    User[user-service :8081] -.-> |读写| DB_User[(user_db)]