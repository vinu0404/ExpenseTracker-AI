# ── Build stage ──
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ── Runtime stage ──
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# JVM tuned for containers:
#  -XX:MaxRAMPercentage=75  → use at most 75% of container memory limit
#  -XX:+UseG1GC            → low-pause garbage collector
#  -XX:+UseStringDeduplication → saves heap on repeated strings
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+UseStringDeduplication", \
  "-jar", "app.jar"]