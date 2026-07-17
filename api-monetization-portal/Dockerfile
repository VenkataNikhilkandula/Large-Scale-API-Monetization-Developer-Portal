# Build stage using Maven and JDK 21
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage using lightweight JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/api-monetization-portal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
