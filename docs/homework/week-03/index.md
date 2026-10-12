# 第3周作业：Spring Boot 工程搭建

## 本周计划
- 在仓库根目录创建 `monolith/` 目录，放入 Spring Boot 工程。
- 使用 Maven 构建，Group 和 Package 统一为 `com.zjgsu.<缩写>`。
- 配置 `application.yml`，端口 8080。
- 实现 GET 接口 `/api/hello` 和健康检查 `/actuator/health`。
- 编写启动测试 `MonolithApplicationTests`。

## 完成情况
- [ ] 工程创建完成
- [ ] `/api/hello` 可访问
- [ ] `/actuator/health` 返回 UP
- [ ] `./mvnw test` 通过
- [ ] `./mvnw spring-boot:run` 启动成功

## 启动命令
```bash
cd monolith
./mvnw spring-boot:run
```

## 测试命令
```bash
cd monolith
./mvnw test
```

## 接口响应
- `GET /api/hello` → `智驾伴侣 monolith 运行正常！`
- `GET /actuator/health` → `{"status":"UP"}`

## 后续计划
下周将在 monolith 工程基础上开始业务建模，实现车辆、告警等核心领域对象。
