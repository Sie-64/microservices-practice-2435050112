# 第6周作业：微服务拆分与通信

## 1. 服务拆分设计

### 实现思路
- **服务边界**：将原单体应用拆分为 `user-service`（用户）、`vehicle-service`（车辆）、`alert-service`（告警）三个独立服务。
- **独立数据库**：每个服务配置了独立的数据库（`user_db`、`vehicle_db`、`alert_db`），严格遵循数据归属原则，不跨库直接查表。
- **跨服务通信**：`alert-service` 在创建告警时，通过 `RestTemplate` 同步调用 `vehicle-service` 的 HTTP 接口校验车辆是否存在。

### 运行命令
（后续补充在 compose 中一键启动，目前为本地独立启动验证）
- `cd services/vehicle-service && ./mvnw spring-boot:run`
- `cd services/alert-service && ./mvnw spring-boot:run`

### 验证结果
- 服务启动成功，分别运行在 8082 和 8083 端口。
- 跨服务调用正常：当车辆不存在时，告警服务成功拦截请求并返回 400 错误。

```markdown
## 1. 服务拆分设计

### 实现思路
按照 DDD（领域驱动设计）思想，将原本的单体系统按业务能力拆分为三个独立的 Spring Boot 服务：`user-service`、`vehicle-service` 和 `alert-service`。
- **数据隔离**：为每个服务分配了独立的数据库（user_db, vehicle_db, alert_db）。
- **通信协作**：`alert-service` 在写入数据前，通过 `RestTemplate` 调用 `vehicle-service` 的接口进行合法性校验。

### 验证结果
已在本地分别启动 `vehicle-service` (8082) 和 `alert-service` (8083)，测试了跨服务调用。若传入不存在的 vehicleId，告警服务能成功拦截并返回 400 错误，验证了业务边界逻辑。