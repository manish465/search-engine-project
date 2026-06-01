FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY common/ /app/common/
COPY shard/ /app/root/

RUN mvn -f /app/common/pom.xml clean install -DskipTests
RUN mvn -f /app/root/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /app/root/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]