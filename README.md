# 智驾伴侣（Smart Drive Companion）

智驾伴侣是一个面向车主的车辆服务平台。项目从单体应用逐步演进为 Spring Boot
微服务，当前实现了用户、车辆、告警、驾驶行为和碳排五个服务，并使用 Nacos
完成服务注册与发现。

## 当前服务

| 服务名 | 宿主机端口 | 数据库 | 职责 |
|---|---:|---|---|
| `user-service` | 8081 | `user_db` | 车主与套餐信息 |
| `vehicle-service` | 8082 | `vehicle_db` | 车辆档案 |
| `alert-service` | 8083 | `alert_db` | 告警记录与等级校验 |
| `driving-behavior-service` | 8084 | `driving_db` | 驾驶事件、评分和月度报告 |
| `carbon-service` | 8085 | `carbon_db` | 碳排计算和历史报告 |

所有业务服务使用 `COURSE_GROUP` 分组，服务名与
[docs/microservices-design.md](docs/microservices-design.md) 保持一致。

## 数据归属与跨服务调用

- 每个服务只读写自己的数据库，不建立跨库外键，也不直接访问其他服务的 Repository。
- `alert-service` 和 `driving-behavior-service` 通过 Nacos 发现
  `vehicle-service`，校验车辆是否存在。
- `carbon-service` 通过 Nacos 发现 `driving-behavior-service`，读取月度驾驶
  汇总后计算碳排，不复制原始驾驶事件。
- 找不到依赖服务的实例时返回 HTTP 503；关联数据不存在时返回 400 或 404。

## 启动

需要 Docker Desktop 和 Java 17（本地测试使用）。在内存较小的 Docker
Desktop 环境中，按“Nacos → 数据库 → 业务服务”的顺序启动，避免多个 JVM
同时初始化造成资源竞争。

先独立启动 Nacos：

```bash
docker compose up -d nacos
```

确认 Nacos 状态为 `healthy`：

```bash
docker compose ps
```

再启动五个数据库：

```bash
docker compose up -d \
  mysql-user mysql-vehicle mysql-alert mysql-driving mysql-carbon
```

最后按依赖顺序启动业务服务：

```bash
docker compose up -d vehicle-service
docker compose up -d user-service alert-service
docker compose up -d driving-behavior-service
docker compose up -d carbon-service
```

首次构建代码时，可对需要构建的服务增加 `--build`。在分配 8GB 以上内存的
Docker Desktop 中，也可以使用 `docker compose up -d --build` 一次启动。

Nacos 控制台：

```text
http://localhost:8848/nacos/
```

当前 Nacos 为本地开发使用的 `standalone` 模式，业务服务在
`http://localhost:8848` 注册。容器内地址由 `NACOS_SERVER_ADDR=nacos:8848`
覆盖。

停止服务但保留数据库和 Nacos 数据：

```bash
docker compose down
```

只有明确需要清空全部数据时才使用 `docker compose down -v`。

## 冒烟测试

启动全部服务后，从仓库根目录执行：

```bash
bash scripts/smoke-test.sh
```

脚本依次检查五个服务的 `/actuator/health`。任一服务返回非 200 时，脚本返回
非零状态。

## 单元与上下文测试

各服务的测试使用 H2 内存数据库，并在测试配置中关闭 Nacos 注册，不依赖真实
MySQL 或 Nacos。进入对应服务目录执行：

```bash
cd services/user-service && ./mvnw test
cd services/vehicle-service && ./mvnw test
cd services/alert-service && ./mvnw test
cd services/driving-behavior-service && ./mvnw test
cd services/carbon-service && ./mvnw test
```

## API 示例

创建车辆，返回结果中的 `id` 用于后续驾驶事件：

```bash
curl -X POST http://localhost:8082/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{"plateNumber":"浙A88888","brand":"BYD"}'
```

录入驾驶事件：

```bash
curl -X POST http://localhost:8084/api/driving-events \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "occurredAt": "2026-11-07T09:00:00",
    "distanceKm": 32.5,
    "fuelLiters": 2.8,
    "energyKwh": 0,
    "harshAccelerationCount": 2,
    "harshBrakingCount": 1,
    "maxSpeedKph": 105
  }'
```

查询驾驶月度报告：

```bash
curl "http://localhost:8084/api/driving-reports/vehicle/1/monthly?month=2026-11"
```

生成碳排报告。碳排服务会通过 Nacos 发现驾驶行为服务：

```bash
curl -X POST http://localhost:8085/api/carbon/reports \
  -H "Content-Type: application/json" \
  -d '{"vehicleId":1,"month":"2026-11"}'
```

查询碳排报告：

```bash
curl http://localhost:8085/api/carbon/reports/vehicle/1
```

## 配置

| 环境变量 | 默认值 | 用途 |
|---|---|---|
| `NACOS_SERVER_ADDR` | `localhost:8848` | Nacos 地址 |
| `MYSQL_USERNAME` | `root` | 新服务的数据库用户 |
| `MYSQL_PASSWORD` | `12345678` | 新服务的数据库密码 |
| `CARBON_GASOLINE_FACTOR` | `2.31` | 每升汽油的 CO₂ 千克数 |
| `CARBON_GRID_FACTOR` | `0.581` | 每千瓦时电力的 CO₂ 千克数 |

生产环境应通过本地 `.env` 或部署环境注入真实凭据，不应把真实密码提交到
Git。第 07 周 Nacos 鉴权、初始化、上下线和截图说明将在后续按要求补充。
