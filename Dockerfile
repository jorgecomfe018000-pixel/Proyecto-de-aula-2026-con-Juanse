# ===== ETAPA 1: COMPILAR =====
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Directorio de trabajo
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Compilar proyecto Spring Boot
RUN mvn clean package -DskipTests

# ===== ETAPA 2: EJECUTAR =====
FROM eclipse-temurin:21-jre-jammy

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR generado desde la etapa build
COPY --from=build /app/target/*.jar app.jar

# Puerto de Spring Boot
EXPOSE 8081

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]