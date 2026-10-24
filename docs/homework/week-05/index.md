# 第5周作业：Dockerfile 与镜像构建

## 构建过程
- 采用多阶段构建，第一阶段基于 `eclipse-temurin:25-jdk` 镜像利用 `./mvnw clean package` 打包生成可执行 JAR。
- 第二阶段基于轻量级 `eclipse-temurin:25-jre` 镜像，仅拷贝 JAR 并执行运行命令。
- 使用 `.dockerignore` 排除了 `target/`、`.idea/` 等无需上传到构建上下文的文件。

## 镜像名称
`lesson-app:latest` （由 compose 自动构建生成）

## 验证结果
执行 `docker build -t smart-car:week05 ./monolith` 构建成功，最终镜像体积远小于完整 JDK 环境镜像。使用 `docker images` 可查看到构建成功的应用镜像。


**② 更新 `docs/homework/week-05/index.md`**：
追加验证过程：
```markdown
## 任务3：业务 API 验证

### 验证过程
应用启动并映射到宿主机 8080 端口后，使用 `curl` 命令进行测试：
1. **健康检查**：访问 `http://localhost:8080/actuator/health`，返回 `200 OK` 及 `{"status":"UP"}`。
2. **创建车辆 (POST)**：向 `http://localhost:8080/api/vehicles` 发送 JSON 数据，成功返回 `200 OK` 及新增数据的 JSON（包含 ID）。
3. **查询车辆 (GET)**：访问 `http://localhost:8080/api/vehicles/1`，成功返回 `200 OK` 及查询到的车辆数据。

证明容器化后的业务接口完全可用。