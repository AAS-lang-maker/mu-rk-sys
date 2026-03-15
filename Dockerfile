# 1. 使用 JDK 17 的轻量级版本作为基础镜像
FROM bellsoft/liberica-openjdk-alpine:17
# 2. 这里的作者信息（可选，写你的名字，职业感拉满！）
LABEL authors="ASS-lang-maker"
# 3. 这里的 /app 是集装箱内部的工作目录
WORKDIR /app
# 4. 【关键一步】把 target 文件夹下生成的那个 jar 包拷进来
# 注意：确保你本地已经执行过 mvn package，target 下有 jar 包
COPY target/*.jar app.jar
# 5. 告诉容器运行的时候，默认去跑这个 Java 程序
ENTRYPOINT ["java", "-jar", "app.jar"]
# 6. 暴露 8080 端口（虽然 compose 也会配，但写在这里是好习惯）
EXPOSE 8080