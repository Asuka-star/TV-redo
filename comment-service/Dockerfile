# -----------------------------------------------
# 阶段 1: 构建 (使用 Maven 和 JDK 11)
# (如果你的项目是 JDK 8 或 17，请修改 '11-openjdk-11')
# -----------------------------------------------
FROM maven:3.8-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 将 Maven 配置文件添加到镜像中，使用阿里云 Maven 镜像源
RUN echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
    <mirrors>\
        <mirror>\
            <id>alimaven</id>\
            <name>aliyun maven</name>\
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>\
            <mirrorOf>central</mirrorOf>\
        </mirror>\
    </mirrors>\
</settings>' > /usr/share/maven/conf/settings.xml

# 复制 pom.xml，利用 Docker 缓存层
# 只有 pom.xml 变化时，才会重新下载依赖
COPY . .
RUN mvn clean package -DskipTests -pl comment-service -am

# -----------------------------------------------
# 阶段 2: 运行 (使用精简的 JRE)
# -----------------------------------------------
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 从 'builder' 阶段复制已打包好的 .jar 文件
# (!! 注意: 这里的 'app.jar' 是你打包后的文件名，请按需修改)
COPY --from=builder /app/comment-service/target/*.jar /app.jar

# 暴露你的服务端口 (比如 Spring Boot 默认的 8080)
EXPOSE 8091

# 容器启动时执行的命令
# (!! 注意: 我们可以通过 Docker Compose 环境变量覆盖 JVM 参数)
ENTRYPOINT ["java", "-jar", "/app.jar"]