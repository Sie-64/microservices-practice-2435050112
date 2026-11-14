# 第8周作业：微服务同步调用与负载均衡

## 1. 选择一个跨服务业务流程

### 实现思路
- **业务场景**：告警创建前校验车辆是否存在。
- **调用方**：`alert-service` (8083)
- **被调用方**：`vehicle-service` (8082)
- **依赖接口**：`GET /api/vehicles/{id}`
- **服务边界**：`alert-service` 不直接查 `vehicle-service` 的库，而是通过 Nacos 发现并调用其接口。配置了 Resilience4j 的 Fallback，当 `vehicle-service` 不可用时，降级类会抛出业务异常，阻断告警落库。

## 2. 使用 OpenFeign

### 实现思路
- **依赖引入**：在 `alert-service` 中引入 `spring-cloud-starter-openfeign` 和 `spring-cloud-starter-loadbalancer`。
- **声明式调用**：在 `VehicleClient` 接口上使用 `@FeignClient(name = "vehicle-service")` 指定服务名，无需关心底层 IP 和端口。
- **失败处理**：配置了 `VehicleClientFallback` 降级类。当目标服务不可用（网络故障或宕机）时，抛出“车辆服务不可用，已触发降级”的异常。

### 运行命令与结果
启动服务后，使用 `curl` 发起告警请求。车辆在线时，Feign 成功调用返回 200；停止车辆服务后，Feign 触发降级，返回“车辆服务不可用”的清晰提示。

## 3. 负载均衡验证

### 实现思路
- **多实例部署**：在 `compose.yaml` 中将 `vehicle-service` 拆分为两个实例，映射宿主机端口分别为 `8082` 和 `8086`。
- **实例标识注入**：通过环境变量 `INSTANCE_NAME` 分别注入 `vehicle-1` 和 `vehicle-2`，并在 `VehicleController` 中打印日志。
- **LoadBalancer 机制**：Spring Cloud LoadBalancer 结合 Nacos 服务发现，自动在调用方进行轮询分发。

### 运行命令与结果
连续发起多次告警请求：
```bash
for i in {1..4}; do
  curl -s -X POST http://localhost:8083/api/alerts -H "Content-Type: application/json" -d '{"vehicleId":2,"alertType":"均衡测试","level":2}'
done