# ===== build stage =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
# 依存DLを先にやってキャッシュを効かせる
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ===== run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Render が渡す $PORT を使って待受ける。ローカル起動時のデフォルトは 8080
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

# 成果物を配置（target に jar が1個だけ前提）
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Render では必ず $PORT で LISTEN すること
# プロファイルは 'render' を使う前提（application-render.yml を用意）
CMD ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT} -Dspring.profiles.active=render -jar app.jar"]
