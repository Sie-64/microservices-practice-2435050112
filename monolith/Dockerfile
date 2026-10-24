# 第一阶段：构建
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
# 拷贝 maven 相关文件
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# 赋予执行权限并下载依赖
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
# 拷贝源码并打包
COPY src ./src
RUN ./mvnw clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:25-jre
WORKDIR /app
# 从构建阶段拷贝打好的 jar 包
COPY --from=builder /app/target/*.jar app.jar
# 暴露 8080 端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]