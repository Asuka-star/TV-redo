# -----------------------------------------------
# 阶段 1: 构建 (使用 Maven 和 JDK 17)
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
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制所有源代码
COPY src ./src

# 运行 Maven 打包
RUN mvn clean package -DskipTests

# -----------------------------------------------
# 阶段 2: 运行 (使用精简的 JRE)
# -----------------------------------------------
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 从 'builder' 阶段复制已打包好的 .jar 文件
COPY --from=builder /app/target/*.jar /app.jar

# 暴露服务端口
EXPOSE 8094

# 容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "/app.jar"]