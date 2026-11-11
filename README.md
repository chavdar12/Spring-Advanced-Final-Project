# TradeZone — classifieds marketplace

Post, search and favorite advertisements, browse by region, and chat with other users in real time.
Spring Boot 4 API + Angular 22 / Angular Material client, behind an nginx API gateway.

```
browser ──► gateway (nginx) ──► server (Spring Boot) ──► PostgreSQL
            │ serves the SPA          │ cache, idempotency ──► Redis
            │ rate limiting           └ STOMP chat over SockJS
            ├ bearer-token check (auth_request → /api/auth/verify)
            └ /kibana/ (basic auth) ──► Kibana ──► Elasticsearch ◄── Logstash ◄── container logs (GELF)
```

## Run with Docker

```bash
cp .env.example .env    # set DB_PASSWORD, JWT_SECRET (openssl rand -base64 64) and KIBANA_PASSWORD
docker compose up -d --build              # http://localhost:8080
GATEWAY_PORT=9000 docker compose up -d    # different host port
```

Only the gateway publishes a port (plus Logstash's GELF input on 127.0.0.1:12201/tcp). Image uploads (profile pictures,
ad photos, categories) need
`CLOUDINARY_*` credentials; without them everything else works, but a profile can't be completed.
The first account registered becomes ROOT, the second ADMIN, the third MODERATOR.

To reach Postgres and Redis from GUI tools (DBeaver, RedisInsight, ...), create a git-ignored
`compose.override.yaml` next to `compose.yaml`; Compose merges it automatically:

```yaml
services:
  postgres:
    ports: ["127.0.0.1:${POSTGRES_PORT:-5432}:5432"]
  redis:
    ports: ["127.0.0.1:${REDIS_PORT:-6379}:6379"]
```

Then `docker compose up -d postgres redis` and connect to `localhost:5432` (database and user
`trade_zone`, password `DB_PASSWORD`) and `localhost:6379` (no password, hence the `127.0.0.1` binding).
Without it: `docker compose exec postgres psql -U trade_zone -d trade_zone` or `docker compose exec redis redis-cli`.

## Run locally

```bash
docker run -d --name tz-pg -p 5432:5432 -e POSTGRES_DB=trade_zone -e POSTGRES_USER=trade_zone \
  -e POSTGRES_PASSWORD=trade_zone postgres:18-alpine
docker run -d --name tz-redis -p 6379:6379 redis:8-alpine
(cd server && ./gradlew bootRun)      # API on :8080 (DB_URL, REDIS_HOST, JWT_SECRET env vars override defaults)
(cd client && npm ci && npm start)    # http://localhost:4200, proxies /api and /wechat to :8080
```

## Tests

```bash
(cd server && ./gradlew build)      # server unit + integration tests (Testcontainers: needs Docker)
(cd client && npm test)             # client unit tests (Vitest)
(cd client && npm run lint && npm run format:check)   # ESLint (angular-eslint) + Prettier
docker compose up -d --build && cd client && npm run e2e   # Playwright against the full stack
```

## How the gateway and API behave

- **Auth**: `POST /api/auth/signin` returns a JWT; send it as `Authorization: Bearer <token>`. nginx rejects
  protected `/api/**` calls with 401 before they reach the server (results cached 30 s per token); the server
  validates the token again. The chat authenticates the STOMP `CONNECT` frame with the same header.
- **Rate limits** (per IP): 20 req/s with a burst of 40 on the API, 10 req/min with a burst of 5 on
  signin/signup. Over the limit → 429.
- **Idempotency**: send `Idempotency-Key: <uuid>` on POST/PUT/PATCH/DELETE. The first response is stored in
  Redis for 24 h and replayed (with `Idempotent-Replayed: true`) for repeats; the same key with a different
  body → 422; a repeat while the first is still running → 409. The client sets the key and retries
  network/502/503/504 failures with it.
- **Errors**: every failure — from the server or from nginx itself (401, 429) — is an RFC 9457
  `application/problem+json` body: `{type, title, status, detail, instance}`, plus `errors: {field: message}`
  when request validation fails. Request DTOs are validated before any work; entities carry matching
  Bean Validation constraints and the schema enforces them again (NOT NULL, UNIQUE, CHECK).
- **Caching**: categories, towns and regions are cached in Redis (regions/towns-by-region for 1 min since
  they carry counts, the rest for 1 h; creating a category evicts its caches).
- **Schema**: Flyway migrations in `server/src/main/resources/db/migration`; Hibernate only validates.
- API docs: `/swagger-ui.html` on the server (not routed through the gateway).

## Logs and usage dashboard (ELK)

Docker's GELF log driver ships the server's and the gateway's stdout to Logstash, which indexes it into
Elasticsearch (`trade-zone-*`). The server logs ECS JSON and nginx logs one JSON line per request (method,
uri, status, user, response time). Open **http://localhost:8080/kibana/** (user `admin`, password
`KIBANA_PASSWORD`). The **Trade Zone usage** dashboard shows API requests, active users, 5xx errors,
requests by status, server log levels, the busiest endpoints and the most active users. `elk-setup`
installs the index template, data view and dashboard on every `docker compose up`.

The GELF address is resolved by the Docker daemon. The default, `host.docker.internal`, works on Docker
Desktop; on a Linux host set `GELF_HOST=localhost`. ELK needs about 2 GB of RAM.

## Layout

```
server/    Spring Boot API (Java 25, Gradle wrapper inside)
client/    Angular 22 + Material SPA (signals, signal forms, httpResource), Vitest, Playwright e2e
           src/app/core      session, HTTP interceptors, notifications
           src/app/shared    reusable UI (confirm dialog) and helpers
           src/app/features  one folder per feature: <feature>-api.ts, models, a folder per component
gateway/   nginx config + image that bundles the built client
elk/       Logstash pipeline, index template and Kibana dashboard (elk/setup)
compose.yaml, .env.example
```
