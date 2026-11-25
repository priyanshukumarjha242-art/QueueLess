# Root Dockerfile for Render / Cloud deployments
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
VOLUME /app/data
COPY --from=build /app/target/queueless-backend-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
