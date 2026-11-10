# 微服务跨服务调用链路设计

## 业务场景
**“车辆告警创建校验”**。在智驾伴侣系统中，用户为某辆车创建告警时，`alert-service` 必须确保该车辆（`vehicleId`）在系统中真实存在，才能将告警记录写入自己的数据库。此场景严格要求服务边界清晰，不能跨库直接查询。

## 调用方与被调用方
- **调用方**：`alert-service` (端口 8083)
- **被调用方**：`vehicle-service` (端口 8082)
- **依赖接口**：`GET /api/vehicles/{id}`

## 服务调用链路图

```mermaid
sequenceDiagram
    participant Client as 客户端/用户
    participant Alert as alert-service (8083)
    participant Nacos as Nacos 注册中心
    participant Vehicle as vehicle-service (8082)

    Client->>Alert: POST /api/alerts (附带 vehicleId)
    
    Alert->>Nacos: 1. 获取 vehicle-service 实例列表
    Nacos-->>Alert: 返回实例 IP 和端口
    
    Alert->>Vehicle: 2. OpenFeign 发起 GET /api/vehicles/{id}
    
    alt 车辆存在
        Vehicle-->>Alert: 返回 200 OK 及车辆 JSON
        Alert->>Alert: 3. 将告警写入本地数据库
        Alert-->>Client: 返回 200 OK 创建成功
    else 车辆不存在 (404)
        Vehicle-->>Alert: 返回 404 Not Found
        Alert-->>Client: 返回 400 错误 (阻止写入)
    else 车辆服务宕机/不可用
        Alert-->>Client: 触发 Fallback 降级，返回 500 错误 (阻止写入)
    end