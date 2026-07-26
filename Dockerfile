# --- ETAPA 1: Construcción (Build) ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiamos primero las dependencias de Maven para usar la caché de Docker
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copiamos el código fuente y construimos el JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- ETAPA 2: Ejecución (Runtime ligero) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el archivo .jar generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
