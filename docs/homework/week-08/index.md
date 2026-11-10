
---

### 📄 文件 2：`docs/homework/week-08/index.md`
（把这个文件的全部内容替换成下面这段）

```markdown
# 第8周作业：微服务同步调用

## 1. 选择一个跨服务业务流程

### 实现思路
- **业务场景**：告警创建前校验车辆是否存在。
- **技术栈**：使用 Spring Cloud OpenFeign 替代之前的 RestTemplate。
- **服务边界**：`alert-service` 不直接查 `vehicle-service` 的库，而是通过 Nacos 发现并调用其 `GET /api/vehicles/{id}` 接口。
- **容错处理**：配置了 Resilience4j 的 Fallback，当 `vehicle-service` 不可用时，降级类会抛出业务异常，阻断告警落库，确保脏数据不产生。

### 运行命令
```bash
# 启动服务
docker compose up -d alert-service vehicle-service

# 测试接口
curl -i -X POST http://localhost:8083/api/alerts \
  -H "Content-Type: application/json" \
  -d '{"vehicleId":1,"alertType":"碰撞","level":2}'