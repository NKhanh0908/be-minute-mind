# ==============================================================================
# STAGE 1: Build the Spring Boot application using Maven and JDK 21
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy only pom.xml first to cache dependencies download
COPY pom.xml .

# Download dependencies offline to optimize docker cache rebuilds
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# STAGE 2: Run the built JAR using lightweight Eclipse Temurin JRE 21
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
