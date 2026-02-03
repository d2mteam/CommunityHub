# CommunityHub

Minimal text-only forum/group app built with Spring Boot 3 (Java 21), PostgreSQL, and SSE notifications.

## Requirements

- Java 21
- PostgreSQL 16+

## Configuration

Update `src/main/resources/application.yaml` as needed. Default settings:

- Database: `jdbc:postgresql://localhost:5432/communityhub`
- Username/password: `admin` / `s3crect`
- Keycloak issuer: `spring.security.oauth2.resourceserver.jwt.issuer-uri` (prod)
- Keycloak JWKS: `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` (prod)
- Keycloak audience: `app.security.jwt.audience`

The API trusts Keycloak-issued JWTs and validates issuer, audience, signature, and expiry.
User identity comes directly from JWT claims (`sub`, `preferred_username`, `email`, roles, scopes).
The `sub` claim must align with the local `users.id` (UUID) used for content ownership. See `SECURITY.md` for details.

## Run

```bash
./gradlew bootRun
```

 Liquibase migrations run automatically on startup.

## Run without Keycloak (DEV)

The DEV profile uses a locally signed **Keycloak-shaped** JWT (same claims/structure as production)
and validates it using a local public key. This keeps the authorization logic identical while
removing the Keycloak runtime dependency for local work.

1) Generate a local RSA keypair (private key is gitignored, public key is committed):

```bash
./scripts/generate-dev-keypair.sh
```

2) Create a `.env` based on `.env.example` and generate a token:

```bash
cp .env.example .env
DEV_TOKEN=$(node ./scripts/generate-dev-token.js)
```

3) Run the API with the dev profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

4) Use the printed token in Postman/curl:

```bash
curl -H "Authorization: Bearer $DEV_TOKEN" http://localhost:8080/me
```

For frontend clients, set `VITE_DEV_TOKEN` in `.env.local` (see `.env.example`) and attach it
as `Authorization: Bearer <token>` on API requests.

> The dev token is only accepted when `SPRING_PROFILES_ACTIVE=dev`. Production uses JWKS
> validation and rejects locally signed tokens.

## Docker Compose (app + Postgres + Keycloak)

```bash
docker-compose up --build
```

This starts:

- CommunityHub API on `http://localhost:8080`
- Keycloak on `http://localhost:8081` (realm: `communityhub`)
- Postgres on `localhost:5432`

Keycloak demo user for local testing:

- Username: `demo`
- Password: `demo1234`

The Keycloak realm includes a `communityhub-api` client with audience mapping enabled so the
issued JWT includes `aud=communityhub-api`.

To exercise write operations, seed a matching user row for the demo account (UUID matches
the Keycloak `sub` claim):

```sql
insert into users (id, username, email)
values ('2d8f2b2c-8c07-4d3f-9a1a-7b54b1d31c3a', 'demo', 'demo@example.com')
on conflict do nothing;
```

## Sample usage

```bash
# Obtain a Keycloak access token (sub must map to users.id)
TOKEN="eyJhbGciOi..."

# Create group
curl -X POST http://localhost:8080/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"slug":"spring","name":"Spring Fans"}'

# Join group
curl -X POST http://localhost:8080/groups/1/join -H "Authorization: Bearer $TOKEN"

# Create post
curl -X POST http://localhost:8080/groups/1/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","body":"First post"}'

# Create comment
curl -X POST http://localhost:8080/posts/1/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"body":"Nice post"}'

# List notifications
curl -X GET http://localhost:8080/notifications \
  -H "Authorization: Bearer $TOKEN"

# SSE stream
curl -N http://localhost:8080/sse/notifications \
  -H "Authorization: Bearer $TOKEN"
```

## Cursor pagination

Endpoints that return lists include `nextCursor` (Base64 encoded `createdAt|id`). Use it as the `cursor` query param to paginate.

## Notes

- Posts/comments are soft-deleted by status.
- Notifications are generated on comment/reply, excluding self-notifications.
- SSE emits `notification` events and `heartbeat` events every ~20 seconds.
