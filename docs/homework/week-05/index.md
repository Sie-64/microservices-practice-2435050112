# 第5周作业：Dockerfile 与镜像构建

## 构建过程
- 采用多阶段构建，第一阶段基于 `eclipse-temurin:25-jdk` 镜像利用 `./mvnw clean package` 打包生成可执行 JAR。
- 第二阶段基于轻量级 `eclipse-temurin:25-jre` 镜像，仅拷贝 JAR 并执行运行命令。
- 使用 `.dockerignore` 排除了 `target/`、`.idea/` 等无需上传到构建上下文的文件。

## 镜像名称
`lesson-app:latest` （由 compose 自动构建生成）

## 验证结果
执行 `docker build -t smart-car:week05 ./monolith` 构建成功，最终镜像体积远小于完整 JDK 环境镜像。使用 `docker images` 可查看到构建成功的应用镜像。