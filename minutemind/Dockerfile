# ================================
# Stage 1: Build
# ================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml trước để tận dụng Docker layer cache khi dependencies chưa đổi
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source và build (bỏ qua test để tăng tốc)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ================================
# Stage 2: Runtime
# ================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Tạo user non-root để chạy app (bảo mật)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR từ stage build
COPY --from=builder /app/target/*.jar app.jar

# Đổi ownership sang appuser
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# JVM tuning cho container: giới hạn heap theo memory container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]