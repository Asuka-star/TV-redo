# -----------------------------------------------
# 阶段 1: 构建 (使用 Maven 和 JDK 11)
# (如果你的项目是 JDK 8 或 17，请修改 '11-openjdk-11')
# -----------------------------------------------
FROM maven:3.8-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml，利用 Docker 缓存层
# 只有 pom.xml 变化时，才会重新下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制所有源代码
COPY src ./src

# 运行 Maven 打包
# (这里我们跳过了测试，因为 Jenkins 应该在前面步骤单独测试)
RUN mvn clean package -DskipTests

# -----------------------------------------------
# 阶段 2: 运行 (使用精简的 JRE)
# -----------------------------------------------
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 从 'builder' 阶段复制已打包好的 .jar 文件
# (!! 注意: 这里的 'app.jar' 是你打包后的文件名，请按需修改)
COPY --from=builder /app/target/*.jar /app.jar

# 暴露你的服务端口 (比如 Spring Boot 默认的 8080)
EXPOSE 8091

# 容器启动时执行的命令
# (!! 注意: 我们可以通过 Docker Compose 环境变量覆盖 JVM 参数)
ENTRYPOINT ["java", "-jar", "/app.jar"]