# 使用 openjdk:17-jdk 作为基础镜像
FROM openjdk:17-jdk

# 设置工作目录
WORKDIR /app

# 将本地的 excel-sql-0.0.1-SNAPSHOT.jar 文件复制到容器内的 /app 目录
COPY ./target/excel-sql-0.0.1-SNAPSHOT.jar /app/excel-sql-0.0.1-SNAPSHOT.jar

# 定义容器启动时执行的命令
CMD ["java", "-Xmx6g", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "-Dserver.port=9999", "/app/excel-sql-0.0.1-SNAPSHOT.jar"]



