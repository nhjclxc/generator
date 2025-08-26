# 使用官方 OpenJDK 11 的 Alpine 版本作为基础镜像
FROM openjdk:11-jdk-slim

# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器中
COPY generator-0.0.1-SNAPSHOT.jar generator-0.0.1-SNAPSHOT.jar

# 创建日志目录（容器内部）
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8099

# 启动命令
# ENTRYPOINT ["java", "-jar", "generator-0.0.1-SNAPSHOT.jar"]
# "sh", "-c" 是用 Shell 来执行一整段命令字符串，支持重定向、变量、管道等 shell 功能。一般用于要使用复杂命令的情况
#ENTRYPOINT ["sh", "-c", "java -jar /app/generator-0.0.1-SNAPSHOT.jar > /app/logs/app.log 2>&1"]
ENTRYPOINT sh -c "java -jar /app/generator-0.0.1-SNAPSHOT.jar > /app/logs/app.log 2>&1"
