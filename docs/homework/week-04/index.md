# 第4周作业：业务建模与数据库持久化

## 实现思路
- 引入 Spring Data JPA、MySQL 和 H2 依赖。
- 定义 `Vehicle` 和 `Alert` 两个实体类。
- 编写 `AlertService` 实现业务规则：告警等级必须在 1-3 之间。
- 使用 DTO 接收请求，Controller 负责 HTTP 路由。
- 主环境使用 MySQL，测试环境使用 H2 内存数据库。

## 启动与测试命令
- 启动项目：`cd monolith && ./mvnw spring-boot:run`
- 运行测试：`cd monolith && ./mvnw test`

## 测试结果
运行测试后，H2 数据库自动建表，业务规则测试通过，正常请求返回 200 并保存数据，非法等级返回 400 错误。

## 构建应用实现

### 实现思路
- **业务模型**：定义了 `Vehicle`（车辆）和 `Alert`（告警）两个 JPA 实体。
- **业务规则**：在 `AlertService` 中实现校验，如果告警级别 `level` 不在 1-3 之间，抛出 `IllegalArgumentException`。
- **REST 接口**：使用 `@RestController` 实现了 4 个接口（均在 `/api` 路径下），包含创建车辆、查询车辆、创建告警、查询告警。
- **分层设计**：Controller 只负责接收 HTTP 请求和返回响应，具体的业务校验全部交由 Service 层处理，没有直接编写数据访问逻辑。

### 运行命令
```bash
cd monolith
./mvnw spring-boot:run