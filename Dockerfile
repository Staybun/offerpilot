# Docker 镜像构建
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY lib ./lib
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/offerpilot-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8101
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
