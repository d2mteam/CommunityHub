# Security Overview

## Trust boundary

CommunityHub **only** trusts JWTs issued by Keycloak. The application does **not** accept
identity information from custom headers or request parameters (for example `x-user-id`,
`x-role`, or similar). All authenticated requests must include:

```
Authorization: Bearer <access_token>
```

If the JWT is missing, invalid, or expired, the request is rejected with `401 Unauthorized`.

## Authentication flow

1. The client performs **OIDC Authorization Code Flow** against Keycloak.
2. Keycloak issues an **access token (JWT)**.
3. The client calls CommunityHub APIs with the access token as a Bearer token.
4. CommunityHub validates the JWT signature (JWKS), issuer (`iss`), audience (`aud`), and
   expiration (`exp`) before handling the request.

> The application does not redirect to login and does not provide `/login` or `/register`
> endpoints.

## JWT claims used

CommunityHub derives a **read-only** user context from JWT claims (no database lookup for identity):

- `sub` → `userId` (UUID)
- `preferred_username` → `username`
- `email` → `email`
- `realm_access.roles` → `roles`
- `scope` or `scp` → `scopes`

The resulting context is used for authorization decisions and for attributing actions in
business logic. When persisting content, CommunityHub expects `sub` to map to the local
`users.id` value (UUID) used for content ownership.

## Authorization

Authorization is based on roles/scopes provided by Keycloak. Role and scope changes in
Keycloak take effect immediately and do **not** require redeploying CommunityHub. Roles
are **not** stored in the application database.

## What the application does NOT do

- **No credential storage** (passwords are not stored).
- **No authentication endpoints** (`/login`, `/register`).
- **No internal JWT signing**.
- **No identity resolution via database** for authenticated requests.

All identity and access management responsibilities are delegated to Keycloak.
