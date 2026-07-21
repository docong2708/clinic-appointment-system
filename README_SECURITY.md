# Security Architecture - Local JWT

The system now uses local account security instead of Google login or Keycloak.

## Runtime Flow

1. Client calls `POST /auth/login` through `api-gateway`.
2. Gateway routes `/auth/**` to `user-service`.
3. `user-service` validates the local user password with BCrypt.
4. `user-service` returns:
   - short-lived JWT access token
   - opaque refresh token
5. Gateway verifies the JWT signature and issuer on protected requests.
6. Gateway injects user context into downstream services, including `GET /auth/me`:
   - `X-User-Id`
   - `X-User-Email`
   - `X-User-Roles`

Downstream services continue to read the current user from `common-security`.

## Token Claims

Access tokens are signed with HS256 using `app.auth.jwt-secret`.

Claims:

```json
{
  "iss": "clinic-appointment-system",
  "sub": "<local users.id UUID>",
  "type": "access",
  "email": "user@example.com",
  "roles": ["PATIENT"]
}
```

`sub` is the local `users.id`, not a Keycloak subject.

## Endpoints

The auth endpoints are implemented by `user-service` and exposed through the gateway route `/auth/**`.

```http
POST /auth/login
Content-Type: application/json

{
  "email": "patient@example.com",
  "password": "secret123"
}
```

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "<refresh-token>"
}
```

```http
GET /auth/me
Authorization: Bearer <access-token>
```

## Configuration

Set the same secret on `api-gateway` and `user-service` from the Spring Cloud Config repository.
Do not keep the JWT secret in each service's local `application.yml`.

`api-gateway.yaml`:

```yaml
app:
  auth:
    jwt-issuer: ${JWT_ISSUER:clinic-appointment-system}
    jwt-secret: '{cipher}<encrypted-jwt-secret>'
    access-token-max-age-seconds: ${ACCESS_TOKEN_MAX_AGE_SECONDS:3600}
    refresh-token-max-age-seconds: ${REFRESH_TOKEN_MAX_AGE_SECONDS:604800}
```

`user-service.yaml`:

```yaml
app:
  auth:
    jwt-issuer: ${JWT_ISSUER:clinic-appointment-system}
    jwt-secret: '{cipher}<encrypted-jwt-secret>'
    access-token-max-age-seconds: ${ACCESS_TOKEN_MAX_AGE_SECONDS:3600}
    refresh-token-max-age-seconds: ${REFRESH_TOKEN_MAX_AGE_SECONDS:604800}
```

The encrypted value must be produced with the same `ENCRYPT_KEY` used by `config-server`.

## Database

`user-service` stores:

- `users.password_hash`
- `refresh_tokens.token_hash`
- `refresh_tokens.expires_at`
- `refresh_tokens.revoked_at`

Refresh tokens are never stored in plaintext. A new refresh token is issued on every refresh, and the old token is revoked.
