# CommunityHub

Minimal text-only forum/group app built with Spring Boot 3 (Java 21), PostgreSQL, and SSE notifications.

## Requirements

- Java 21
- PostgreSQL 16+

## Configuration

Update `src/main/resources/application.yaml` as needed. Default settings:

- Database: `jdbc:postgresql://localhost:5432/communityhub`
- Username/password: `admin` / `s3crect`
- Keycloak issuer: `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- Keycloak audience: `app.security.jwt.audience`

The API trusts Keycloak-issued JWTs and validates issuer, audience, signature, and expiry.
User identity comes directly from JWT claims (`sub`, `preferred_username`, `email`, roles, scopes).
The `sub` claim must align with the local `users.id` used for content ownership. See `SECURITY.md` for details.

## Run

```bash
./gradlew bootRun
```

Flyway migrations run automatically on startup.

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
