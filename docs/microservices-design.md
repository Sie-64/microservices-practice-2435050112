# 智驾伴侣 - 微服务拆分设计

## 1. 单体模块到微服务的拆分映射
原单体架构（`monolith`）中包含车辆管理与告警管理两大核心模块。现按业务边界拆分如下：
- 原单体中的 `Vehicle` 模块及相关 CRUD 拆分至 **`vehicle-service`**。
- 原单体中的 `Alert` 模块及相关逻辑拆分至 **`alert-service`**。
- 新增 **`user-service`** 用于管理车主账号与订阅套餐。
- 新增 **`driving-behavior-service`** 用于保存驾驶事件并生成驾驶月报。
- 新增 **`carbon-service`** 用于生成车辆碳排报告。

## 2. 服务职责、端口与数据归属
遵循“每个服务只读写自己的数据”原则，各服务拥有独立数据库。

| 服务名 | 端口 | 职责 | 独立数据库 | 拥有的数据表 |
|--------|------|------|------------|--------------|
| user-service | 8081 | 车主信息、套餐等级管理 | user_db | users |
| vehicle-service | 8082 | 车辆档案、车牌、品牌管理 | vehicle_db | vehicles |
| alert-service | 8083 | 车辆告警记录、级别管理 | alert_db | alerts |
| driving-behavior-service | 8084 | 驾驶事件、行为评分、月度报告 | driving_db | driving_events |
| carbon-service | 8085 | 月度碳排计算和历史报告 | carbon_db | carbon_reports |

> **关键约束**：跨服务关联仅保存对方的 ID（如 alert 表保存 vehicleId），
> 不建立跨库外键，各服务禁止直接调用对方的 Repository。

## 3. 服务之间的调用关系
所有远程调用都先通过 Nacos `DiscoveryClient` 获取服务实例，再使用实例 IP 和
端口发起 HTTP 请求，不依赖固定容器地址。

- `alert-service` 创建告警前发现 `vehicle-service`，校验车辆存在。
- `driving-behavior-service` 录入驾驶事件前发现 `vehicle-service`，校验车辆存在。
- `carbon-service` 生成报告时发现 `driving-behavior-service`，读取指定车辆和月份的驾驶汇总。

依赖服务没有可用实例时返回 HTTP 503；车辆或驾驶数据不存在时拒绝生成业务数据。

## 4. Mermaid 架构图

```mermaid
graph TD
    Client[客户端 / 前端]
    
    subgraph 微服务集群
        User[user-service :8081]
        Vehicle[vehicle-service :8082]
        Alert[alert-service :8083]
        Driving[driving-behavior-service :8084]
        Carbon[carbon-service :8085]
    end

    subgraph 独立数据库
        DB_User[(user_db)]
        DB_Vehicle[(vehicle_db)]
        DB_Alert[(alert_db)]
        DB_Driving[(driving_db)]
        DB_Carbon[(carbon_db)]
    end

    Client --> |HTTP| User
    Client --> |HTTP| Vehicle
    Client --> |HTTP| Alert
    Client --> |HTTP| Driving
    Client --> |HTTP| Carbon

    Alert -.-> |服务发现 + 车辆校验| Vehicle
    Driving -.-> |服务发现 + 车辆校验| Vehicle
    Carbon -.-> |服务发现 + 驾驶月报| Driving
    
    User -.-> |读写| DB_User
    Vehicle -.-> |读写| DB_Vehicle
    Alert -.-> |读写| DB_Alert
    Driving -.-> |读写| DB_Driving
    Carbon -.-> |读写| DB_Carbon

