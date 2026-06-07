# Environment Variables

This document catalogs all environment variables used by the MinuteMind backend to modify configuration across local, staging, and production environments.

These variables are defined in [application.properties](file:///d:/Working/Project/personal-project/minute-mind/be-minute-mind/minutemind/src/main/resources/application.properties) and can be loaded via a `.env` file when running via Docker Compose.

---

## Configuration Reference Table

| Variable Name | Default Value (Local/Dev) | Purpose | Production Considerations |
| :--- | :--- | :--- | :--- |
| **`CONTEXT_PATH`** | `/api/v1` | Context path prefix for all API endpoints. | Usually kept as `/api/v1`. |
| **`DATABASE_URL`** | `jdbc:postgresql://localhost:5432/minutemind_dev` | JDBC URL for PostgreSQL database connection. | Point to your managed production DB (e.g. Supabase pooler). |
| **`DB_USERNAME`** | `postgres` | Database login username. | Production DB username. |
| **`DB_PASSWORD`** | `postgres` | Database login password. | Keep secret. Use strong passwords. |
| **`MAX_POOL_SIZE`** | `10` | Maximum Hikari Connection Pool size. | Increase if handling high concurrent traffic. |
| **`MIN_IDLE`** | `2` | Minimum idle connections kept in the pool. | Optimize based on server memory. |
| **`CONNECTION_TIMEOUT`**| `30000` | Hikari database connection timeout in milliseconds. | Kept as default (`30s`). |
| **`JPA_DDL_AUTO`** | `update` (Dev) / `validate` (Prod) | Hibernate DDL generation strategy. | **Must** set to `validate` or `none` in production to prevent data loss. |
| **`REDIS_HOST`** | `localhost` | Hostname of the Redis server. | Set to `redis` when running in Docker Compose network, or external host. |
| **`REDIS_PORT`** | `6379` | Port number of the Redis server. | Kept as `6379`. |
| **`REDIS_PASSWORD`** | *Empty* | Password to authenticate with Redis. | Required for secured production Redis instances. |
| **`JWT_SECRET`** | `fDVidgXxPsgxmQnUPMECjPMLRJAeVZV9YuZpOCoOoBo=` | **Base64-encoded** secret key used to sign access/refresh tokens. | **Must change in production**. Must be a cryptographically strong Base64 string. |
| **`JWT_TOKEN_EXPIRY`** | `900000` (15 Minutes) | Lifespan of the Access Token in milliseconds. | Kept short for security. |
| **`JWT_FTOKEN_EXPIRY`**| `604800000` (7 Days) | Lifespan of the Refresh Token in milliseconds. | Controls how long users stay logged in. |
| **`CORS_ORIGINS`** | `http://localhost:5173` | List of allowed client domains separated by commas. | **Must include** the production URL of your frontend (e.g. Vercel domain). |
| **`CLOUDINARY_CLOUD_NAME`**| `ddeucv71n` | Cloudinary cloud identifier name. | Production account cloud name. |
| **`CLOUDINARY_API_KEY`** | `143313557212593` | Cloudinary access API key credentials. | Keep secure. |
| **`CLOUDINARY_API_SECRET`**| `mq2TXZwBgvGBjl1rcr45r23R8b0` | Cloudinary access API secret credentials. | **Keep extremely secure**. |
| **`CLOUDINARY_FOLDER`** | `mintutes-mind` | Folder name where uploaded avatars are stored in Cloudinary. | Separates dev/production assets (e.g. `minute-mind/prod`). |
