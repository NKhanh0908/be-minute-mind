# Docker Deployment Guide

The project provides built-in support for containerization via a multi-stage **Dockerfile** and a ready-to-run **Docker Compose** stack.

---

## 1. Understanding the Dockerfile

The [Dockerfile](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/Dockerfile) uses a **two-stage build process** to minimize final image size and enforce security best practices:

- **Stage 1: Builder (`maven:3.9.6-eclipse-temurin-21-alpine`)**
  - Copies `pom.xml` and downloads Maven dependencies offline (`mvn dependency:go-offline`) to utilize Docker layer caching.
  - Copies source code and compiles the project into a JAR file, skipping test suites (`mvn clean package -DskipTests`) to speed up deployment builds.
- **Stage 2: Runtime (`eclipse-temurin:21-jre-alpine`)**
  - Uses a lightweight JRE runtime environment.
  - Creates a dedicated non-root user and group (`appuser` / `appgroup`) to run the application, reducing vulnerability attack surfaces.
  - Passes container-aware JVM tuning flags to automatically scale heap memory limits relative to container resource boundaries:
    ```bash
    -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
    ```

---

## 2. Running the Stack locally via Docker Compose

The [docker-compose.yml](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/docker-compose.yml) configures a network containing the Spring Boot application and an Alpine Redis container.

### Step 1: Create a `.env` File
In the directory containing `docker-compose.yml`, create a `.env` file containing your environment overrides:
```env
DATABASE_URL=jdbc:postgresql://<db-host>:5432/<db-name>
DB_USERNAME=postgres
DB_PASSWORD=secret_password
JWT_SECRET=fDVidgXxPsgxmQnUPMECjPMLRJAeVZV9YuZpOCoOoBo=
CORS_ORIGINS=http://localhost:5173,https://your-app.vercel.app
CLOUDINARY_CLOUD_NAME=my_cloud
CLOUDINARY_API_KEY=12345
CLOUDINARY_API_SECRET=my_secret
```

### Step 2: Launch the Containers
Run the stack under detached mode:
```bash
docker compose up -d --build
```
This command:
1. Downloads the Alpine Redis image, configures a health check (`redis-cli ping`), and mounts a persistent volume `redis-data`.
2. Builds the Spring Boot container, overrides `REDIS_HOST=redis` to connect internally, loads settings from `.env`, and waits until Redis is healthy (`depends_on`).

### Step 3: Check Logs
```bash
docker compose logs -f app
```

### Step 4: Stop the Stack
```bash
docker compose down
```

---

## 3. Production Deployment Notes
- **DDL Validation**: Ensure `JPA_DDL_AUTO=validate` is loaded in your production `.env` to prevent Hibernate from executing structural migrations automatically.
- **SSL Termination**: Put a reverse proxy like Nginx or Traefik in front of the application port `8080` to handle HTTPS traffic.
