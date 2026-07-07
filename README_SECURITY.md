# Security Architecture - Keycloak

## Responsibilities

`Keycloak`

- Single Identity Provider for the system.
- Issues access tokens.
- Owns credentials, login, password policy, and refresh token lifecycle.
- Defines realm roles: `ADMIN`, `DOCTOR`, `PATIENT`.

`infra/api-gateway`

- Validates Keycloak JWT using Spring Security OAuth2 Resource Server.
- Extracts claims from the validated token.
- Injects downstream identity headers:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Roles`
- Performs coarse role authorization.
- Does not create JWT and does not access `user_db`.

`shared/common-security`

- Provides `CurrentUser`, `CurrentUserHolder`, and `CurrentUserHeaderFilter`.
- Downstream services use it to read `X-User-*` headers.
- Does not parse JWT, create JWT, or access any database.

`services/user-service`

- Does not create JWT.
- Does not login users.
- Manages local user profile data and maps each profile to `keycloak_user_id`.
- Optional public registration endpoint can create a Keycloak user through Keycloak Admin API, then persist the local profile mapping.

Downstream services

- Do not parse JWT.
- Do not create JWT.
- Do not query `user_db`.
- Read current user from `CurrentUserHolder` and enforce business ownership rules locally.

## Local Keycloak

Start Keycloak:

```powershell
cd infra/keycloak
docker compose up -d
```

Admin console:

```text
http://localhost:9080
admin / admin
```

Realm:

```text
clinic-appointment
```

Issuer used by API Gateway:

```text
http://localhost:9080/realms/clinic-appointment
```

Gateway config:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:9080/realms/clinic-appointment}
```

## Token Example

```powershell
curl.exe -X POST http://localhost:9080/realms/clinic-appointment/protocol/openid-connect/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password" `
  -d "client_id=clinic-web" `
  -d "username=admin@clinic.local" `
  -d "password=admin123"
```

Call gateway:

```powershell
curl.exe http://localhost:8080/auth/me `
  -H "Authorization: Bearer <access_token>"
```

## Optional Registration Through user-service

Endpoint:

```text
POST /api/users/register
```

This endpoint creates a Keycloak user and then creates the local profile mapping.

```powershell
curl.exe -X POST http://localhost:8080/api/users/register `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"patient1@example.com\",\"password\":\"password123\",\"fullName\":\"Patient One\",\"phoneNumber\":\"0900000001\",\"role\":\"PATIENT\"}"
```

For this to work, the Keycloak client `clinic-user-service` must have service-account permissions to manage users:

- `realm-management` client role `manage-users`
- `realm-management` client role `view-users`
- `realm-management` client role `view-realm`

## Header Contract

API Gateway injects:

```text
X-User-Id: <keycloak subject>
X-User-Email: <email or preferred_username>
X-User-Roles: ADMIN,DOCTOR,PATIENT
```

Downstream services use:

```java
CurrentUser currentUser = CurrentUserHolder.require();
```

## Important Boundary

Correct flow:

```text
Keycloak issues token
API Gateway validates token
API Gateway forwards X-User-* headers
Services read CurrentUserHolder
```

Incorrect flow:

```text
user-service creates JWT
services parse JWT directly
services query user_db for authentication
```
