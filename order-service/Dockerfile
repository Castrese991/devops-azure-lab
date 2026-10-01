FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY order-service/src order-service/src
RUN mvn -B -pl order-service -am package -DskipTests
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && useradd --uid 10001 --create-home app
WORKDIR /app
COPY --from=build /build/order-service/target/order-service-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
