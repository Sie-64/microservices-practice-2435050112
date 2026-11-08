# 第 7 周作业：Nacos 服务注册与发现

## 1. 完成情况

第 06 周已拆分的五个服务继续保留在 `services/` 目录中。本周在根目录
`compose.yaml` 中接入 Nacos 3.2.4，将 `alert-service`、
`driving-behavior-service` 和 `carbon-service` 的跨服务调用改为通过
`DiscoveryClient` 获取实例地址，并完成正常调用、服务下线和恢复验证。

本周未引入 Nacos Config Starter。五个服务均使用根项目 BOM 管理的
`spring-cloud-starter-alibaba-nacos-discovery`，没有单独覆盖 Nacos Client
版本。

## 2. Nacos 部署与持久化

Nacos 使用 `nacos/nacos-server:v3.2.4` 的 `standalone` 模式。控制台、HTTP
服务端和 SDK gRPC 端口分别为：

| 用途 | 宿主机端口 | 容器端口 |
|---|---:|---:|
| Nacos 控制台 | 18080 | 8080 |
| Nacos HTTP API | 8848 | 8848 |
| Nacos SDK gRPC | 9848 | 9848 |

Nacos 启用 Token 鉴权、Server identity 和管理员鉴权。真实 Token、identity、
Nacos 管理员密码和 MySQL 密码只写入本地 `.env`；仓库中的 `.env.example`
只保留占位值，`.gitignore` 已排除 `.env`。

持久化分为三部分：

| Docker 命名卷 | 内容 |
|---|---|
| `nacos_mysql_data` | Nacos 的 MySQL 配置数据 |
| `nacos_server_data` | Nacos 服务端运行数据 |
| `nacos_server_logs` | Nacos 日志 |

Nacos 的 MySQL 初始化脚本位于 `docker/nacos/mysql-schema.sql`。首次启动命令：

```bash
bash scripts/prepare-nacos-env.sh
docker compose up -d mysql-nacos nacos
bash scripts/init-nacos-admin.sh
```

初始化脚本从 Nacos 管理 API 获取管理员密码，写回本地 `.env`，并实际登录
验证 `accessToken`。重复执行时，如果本地凭据仍有效，不会再次重置密码。

## 3. 服务注册结果

五个业务服务使用与 `docs/microservices-design.md` 一致的服务名，并统一注册到
`COURSE_GROUP`。2026-11-07 通过 Nacos 控制台 API 核对到 5 个服务、每个服务
1 个健康实例：

| 服务名 | Nacos 分组 | 宿主机端口 | 本次实例地址 | 状态 |
|---|---|---:|---|---|
| `user-service` | `COURSE_GROUP` | 8081 | `172.18.0.10:8081` | 健康 |
| `vehicle-service` | `COURSE_GROUP` | 8082 | `172.18.0.9:8082` | 健康 |
| `alert-service` | `COURSE_GROUP` | 8083 | `172.18.0.11:8083` | 健康 |
| `driving-behavior-service` | `COURSE_GROUP` | 8084 | `172.18.0.12:8084` | 健康 |
| `carbon-service` | `COURSE_GROUP` | 8085 | `172.18.0.13:8085` | 健康 |

表中的容器 IP 是本次运行的动态结果，重启 Compose 后可能变化，不能作为固定
调用地址写入配置。服务名、分组和宿主机发布端口保持稳定。

## 4. 服务发现与业务调用

- `alert-service` 创建告警前，通过 Nacos 发现 `vehicle-service`，再用发现到的
  实例地址校验车辆。
- `driving-behavior-service` 录入驾驶事件前，通过 Nacos 发现
  `vehicle-service`，校验车辆是否存在。
- `carbon-service` 生成碳排报告前，通过 Nacos 发现
  `driving-behavior-service`，读取该车辆和月份的驾驶汇总。
- 实例列表为空时，业务请求返回 HTTP 503；固定服务地址不再作为调用入口。

各服务仍只访问自己的数据库，跨服务只传车辆或月份等业务参数，没有建立跨库
外键，也没有读取其他服务的 Repository。

## 5. 验证结果

五个服务的测试都从各自目录执行 `./mvnw test`，使用 H2 内存数据库并设置
`spring.cloud.nacos.discovery.enabled=false`，不依赖真实 MySQL 或 Nacos。
本次共执行 11 个测试，全部通过：

| 服务 | 测试数 | 结果 |
|---|---:|---|
| `user-service` | 1 | 通过 |
| `vehicle-service` | 1 | 通过 |
| `alert-service` | 2 | 通过 |
| `driving-behavior-service` | 4 | 通过 |
| `carbon-service` | 3 | 通过 |

从仓库根目录执行 `bash scripts/smoke-test.sh`，5 个业务健康接口和 Nacos
控制台均返回 HTTP 200。随后执行 `bash scripts/verify-discovery.sh`，得到：

```text
正常调用：HTTP 200
停止 vehicle-service、实例下线：HTTP 503
恢复 vehicle-service、实例重新注册：HTTP 200
```

以上结果说明 Nacos 注册、服务发现、空实例处理和恢复后重新发现均已在真实运行
环境中验证。
