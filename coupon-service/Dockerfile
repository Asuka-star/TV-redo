FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 从 'builder' 阶段复制已打包好的 .jar 文件
# (!! 注意: 这里的 'app.jar' 是你打包后的文件名，请按需修改)
COPY ./target/*.jar /app.jar

# 暴露你的服务端口 (比如 Spring Boot 默认的 8080)
EXPOSE 8092

# 容器启动时执行的命令
# (!! 注意: 我们可以通过 Docker Compose 环境变量覆盖 JVM 参数)
ENTRYPOINT ["java", "-jar", "/app.jar"]