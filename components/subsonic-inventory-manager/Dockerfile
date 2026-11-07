# Etapa 1: Compilación con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera para ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/inventory-manager-*.jar app.jar

EXPOSE 8081

ENV SERVER_PORT=8081
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-inventory:5432/inventory_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres

ENTRYPOINT ["java", "-jar", "app.jar"]
