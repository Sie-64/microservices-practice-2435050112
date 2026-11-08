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

需要 Docker Desktop、JDK 17 或更高版本、`curl` 和 `python3`。在内存较小的
Docker Desktop 环境中，按“Nacos → 数据库 → 业务服务”的顺序启动，避免多个
JVM 同时初始化造成资源竞争。首次启动时，应等 Nacos 初始化完成后再启动业务
服务，否则注册请求会因 Nacos 尚未就绪而失败。

先准备本地环境变量。该步骤会生成 Nacos Token、Server identity 和数据库密码，
写入被 `.gitignore` 排除的 `.env`：

```bash
bash scripts/prepare-nacos-env.sh
```

独立启动 Nacos 的持久化 MySQL 和 Nacos 3.2.4：

```bash
docker compose up -d mysql-nacos nacos
```

确认 Nacos 状态为 `healthy`：

```bash
docker compose ps
```

首次初始化管理员 `nacos` 的密码。Nacos 生成的新密码会写回本地 `.env`，脚本可
重复执行；已有有效凭据时不会再次重置密码：

```bash
bash scripts/init-nacos-admin.sh
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

打开 Nacos 控制台：

```text
http://localhost:18080/
```

Nacos 3 的端口职责：

| 用途 | 宿主机端口 | 容器端口 |
|---|---:|---:|
| 控制台 | 18080 | 8080 |
| 服务端 HTTP API | 8848 | 8848 |
| SDK gRPC | 9848 | 9848 |

业务服务通过容器网络地址 `nacos:8848` 连接 Nacos，统一使用 `COURSE_GROUP`
分组。注册成功后，控制台的“服务管理 → 服务列表”应显示
`user-service`、`vehicle-service`、`alert-service`、
`driving-behavior-service` 和 `carbon-service`，实例地址由 Nacos 动态记录，
不能在配置中写死。

Nacos 配置数据保存在 MySQL 命名卷 `nacos_mysql_data`，服务端数据和日志分别
保存在 `nacos_server_data`、`nacos_server_logs`。服务只引入课程 BOM 管理的
Discovery Starter，本周不引入 Config Starter。

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

脚本检查 Nacos 控制台和五个服务的健康接口。任一项返回非 200 时，脚本返回非零
状态。

验证正常调用、服务下线和恢复：

```bash
bash scripts/verify-discovery.sh
```

该脚本会创建一辆测试车辆，确认 `alert-service` 能通过 Nacos 找到
`vehicle-service` 并返回 HTTP 200；随后短暂停止车辆服务，确认实例列表为空时
返回 HTTP 503；最后自动恢复车辆服务，并再次验证 HTTP 200。

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
| `NACOS_SERVER_ADDR` | `nacos:8848` | Compose 中业务服务连接 Nacos 的地址 |
| `NACOS_CONSOLE_URL` | `http://localhost:18080` | Nacos 控制台地址 |
| `NACOS_USERNAME` | `nacos` | 服务注册使用的 Nacos 管理员 |
| `NACOS_PASSWORD` | 无 | 初始化脚本写入本地 `.env` |
| `NACOS_GROUP` | `COURSE_GROUP` | 所有业务服务使用的 Nacos 分组 |
| `NACOS_AUTH_TOKEN` | 无 | Nacos JWT Token 密钥 |
| `NACOS_AUTH_IDENTITY_KEY` | 无 | Nacos Server identity key |
| `NACOS_AUTH_IDENTITY_VALUE` | 无 | Nacos Server identity value |
| `MYSQL_NACOS_USER` | `nacos` | Nacos 持久化数据库用户 |
| `MYSQL_NACOS_PASSWORD` | 无 | Nacos 持久化数据库密码 |
| `MYSQL_ROOT_PASSWORD` | 无 | 五个业务数据库的 root 密码 |
| `MYSQL_USERNAME` | `root` | 业务数据库用户 |
| `CARBON_GASOLINE_FACTOR` | `2.31` | 每升汽油的 CO₂ 千克数 |
| `CARBON_GRID_FACTOR` | `0.581` | 每千瓦时电力的 CO₂ 千克数 |

如果把业务服务直接运行在宿主机而不是 Compose 中，应把
`NACOS_SERVER_ADDR` 改为 `localhost:8848`。

生产环境应通过本地 `.env` 或部署环境注入真实凭据，不应把真实密码提交到
Git。`.env.example` 只包含占位值。详细部署和验证记录见
[docs/homework/week-07/index.md](docs/homework/week-07/index.md)。

## 自动化测试与验证

### 1. 健康检查脚本
在仓库根目录执行以下命令一键检查 Nacos 控制台与所有微服务的健康状况：
```bash
bash scripts/smoke-test.sh
markdown
该脚本会检查 Nacos 控制台 (18080) 的 HTTP 可访问性，以及 5 个微服务的 /actuator/health 接口。只要任一项返回非 200 状态码，脚本就会立即打印错误并以非零状态码（exit 1）退出，防止将错误忽略。

2. 服务发现与容错验证
通过运行以下脚本，可以一键体验“正常调用 -> 服务下线报错 -> 服务恢复”的完整流程：

bash
bash scripts/verify-discovery.sh
该脚本会自动创建测试车辆，并验证 alert-service 能否通过 Nacos 动态发现 vehicle-service。

3. Nacos 服务列表查看
启动所有业务服务后，请在浏览器访问控制台 http://localhost:18080/，登录后进入“服务管理” -> “服务列表”，即可查看 COURSE_GROUP 分组下的 5 个服务及其动态分配的实例 IP 与端口。

4. 环境隔离测试
各微服务的测试类均在 src/test 目录下，为了确保测试不依赖真实环境：

测试配置中已强制关闭 Nacos 自动注册（spring.cloud.nacos.discovery.enabled=false）。

使用 H2 内存数据库替代真实 MySQL。

使用 Mockito 模拟远程响应。

进入特定服务目录执行测试的命令如下：

bash
cd services/alert-service
./mvnw test