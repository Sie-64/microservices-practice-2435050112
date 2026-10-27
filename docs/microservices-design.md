# 智驾伴侣 - 微服务拆分设计

## 1. 单体模块到微服务的拆分映射
原单体架构（`monolith`）中包含车辆管理与告警管理两大核心模块。现按业务边界拆分如下：
- 原单体中的 `Vehicle` 模块及相关 CRUD 拆分至 **`vehicle-service`**。
- 原单体中的 `Alert` 模块及相关逻辑拆分至 **`alert-service`**。
- 新增 **`user-service`** 用于管理车主账号与订阅套餐。

## 2. 服务职责、端口与数据归属
遵循“每个服务只读写自己的数据”原则，各服务拥有独立数据库。

| 服务名 | 端口 | 职责 | 独立数据库 | 拥有的数据表 |
|--------|------|------|------------|--------------|
| user-service | 8081 | 车主信息、套餐等级管理 | user_db | users |
| vehicle-service | 8082 | 车辆档案、车牌、品牌管理 | vehicle_db | vehicles |
| alert-service | 8083 | 车辆告警记录、级别管理 | alert_db | alerts |

> **关键约束**：跨服务关联仅保存对方的 ID（如 alert 表保存 vehicleId），不建立跨库外键，各服务禁止直接调用对方的 Repository。

## 3. 服务之间的调用关系
当 `alert-service` 接收到创建告警的请求时，不能盲目信任前端传来的 `vehicleId`。它会通过 `RestTemplate` 发起 HTTP GET 请求，调用 `vehicle-service` 的 `GET /api/vehicles/{id}` 接口，校验该车辆是否真实存在。如果车辆不存在，则拒绝写入告警库。

## 4. Mermaid 架构图

```mermaid
graph TD
    Client[客户端 / 前端]
    
    subgraph 微服务集群
        User[user-service :8081]
        Vehicle[vehicle-service :8082]
        Alert[alert-service :8083]
    end

    subgraph 独立数据库
        DB_User[(user_db)]
        DB_Vehicle[(vehicle_db)]
        DB_Alert[(alert_db)]
    end

    Client --> |HTTP| User
    Client --> |HTTP| Vehicle
    Client --> |HTTP| Alert

    Alert -.-> |RestTemplate 远程校验| Vehicle
    
    User -.-> |读写| DB_User
    Vehicle -.-> |读写| DB_Vehicle
    Alert -.-> |读写| DB_Alert

