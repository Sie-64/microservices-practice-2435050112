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