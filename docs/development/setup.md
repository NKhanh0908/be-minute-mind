# Local Setup Guide

Follow this guide to configure your local development environment and run the MinuteMind backend.

---

## Prerequisites

Before starting, ensure you have the following installed on your system:
- **Java Development Kit (JDK) 21**: Recommended distributions are Eclipse Temurin or Amazon Corretto.
- **Apache Maven 3.9+**: For dependency management and builds (or use the packaged Maven Wrapper `mvnw`).
- **PostgreSQL 16+**: Relational database.
- **Redis 7+**: Key-value caching store.
- **Git**: For version control.

---

## 1. Clone & Import the Repository

Clone the project to your local machine:
```bash
git clone <repository-url>
cd be-minute-mind/minutemind
```

Import the project into your IDE (IntelliJ IDEA is recommended):
- Open IntelliJ IDEA ➔ **File** ➔ **Open** ➔ Select the `be-minute-mind/minutemind` directory.
- Allow IntelliJ to import the Maven dependencies defined in `pom.xml`.

---

## 2. Set Up Infrastructure Services (Local)

### PostgreSQL Database
1. Connect to your local PostgreSQL server:
   ```bash
   psql -U postgres
   ```
2. Create the development database:
   ```sql
   CREATE DATABASE minutemind_dev;
   ```
3. Verify connection settings. The default configuration in `application.properties` connects to:
   - Host: `localhost:5432`
   - Database: `minutemind_dev` (Note: default is set to Supabase unless overridden via environment variables).

### Redis Store
Start a local Redis instance on port `6379`.
- **macOS (Homebrew)**:
  ```bash
  brew services start redis
  ```
- **Windows (WSL / Docker)**:
  ```bash
  docker run --name minutemind-redis -p 6379:6379 -d redis:7-alpine
  ```

---

## 3. Run the Application

### Option A: From your IDE (Recommended)
1. Locate the entrypoint class: [MinutemindApplication.java](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/src/main/java/com/be/minutemind/MinutemindApplication.java).
2. Right-click the file and select **Run 'MinutemindApplication'**.
3. *Tip*: Set environment variables under **Run/Debug Configurations** in IntelliJ to customize database or Redis hosts.

### Option B: From the Terminal
Use the Maven Wrapper script:
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
chmod +x mvnw
./mvnw spring-boot:run
```

Once started successfully, the API will be accessible at:
- Base URL: `http://localhost:8080/api/v1`
- Swagger UI Documentation: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI Specification JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`
