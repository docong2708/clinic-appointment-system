# Security Architecture

## Responsibilities

`services/user-service`

- Owns register, login, refresh token, and logout.
- Hashes passwords with BCrypt.
- Issues HS256 JWT access tokens.
- Stores refresh tokens in PostgreSQL table `refresh_tokens`.
- Owns `users`, `roles`, `user_roles`, and `refresh_tokens`.

`shared/common-security`

- Provides shared JWT parsing and validation.
- Provides `JwtProperties`, `JwtClaims`, and `JwtTokenValidator`.
- Provides `CurrentUser`, `CurrentUserHolder`, and a servlet filter that reads `X-User-*` headers.
- Does not login users, issue tokens, or access any database.

`infra/api-gateway`

- Validates JWT for private endpoints.
- Skips validation for public auth endpoints and actuator endpoints.
- Extracts JWT claims and forwards downstream headers:
  - `X-User-Id`
  - `X-User-Email`
  - `X-User-Roles`
- Does not access `user_db` and does not create JWT.

Downstream services

- `patient-service`, `doctor-service`, `appointment-service`, and `notification-service` do not login or create JWT.
- They read `X-User-*` headers through `CurrentUserHolder`.
- Business ownership checks remain inside each service.

## JWT

Access token:

- Algorithm: HS256
- Expiration: 15 minutes
- Claims:
  - `sub`: user ID
  - `email`
  - `roles`
  - `iat`
  - `exp`

Refresh token:

- Secure random URL-safe token
- Expiration: 7 days
- Stored in `refresh_tokens`
- Rotated on refresh
- Revoked on logout

Use the same secret in `user-service` and `api-gateway`:

```powershell
$env:JWT_SECRET="replace-with-a-strong-at-least-32-byte-secret-value"
```

## Curl Examples

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient1@example.com",
    "password": "password123",
    "fullName": "Patient One",
    "phoneNumber": "0900000001",
    "role": "PATIENT"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient1@example.com",
    "password": "password123"
  }'
```

Refresh token:

```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh-token>"
  }'
```

Logout:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh-token>"
  }'
```

Get current user profile:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <access-token>"
```

Admin get all users:

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <admin-access-token>"
```

Patient create appointment:

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Authorization: Bearer <patient-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"doctorId":"<doctor-id>","appointmentTime":"2026-06-06T09:00:00"}'
```

## Gateway Authorization Summary

Public:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`
- `/actuator/**`

Gateway role checks:

- `ADMIN`: `/api/users/**`
- `PATIENT`: `POST /api/appointments`, `GET /api/appointments/my`
- `DOCTOR`: `GET /api/doctors/me/**`, `GET /api/appointments/doctor/**`
- `ADMIN` or `DOCTOR`: schedule paths
- `ADMIN` or `PATIENT`: patient profile paths

Detailed ownership rules, such as a patient only reading their own appointment, must be enforced inside the downstream service using `CurrentUserHolder`.