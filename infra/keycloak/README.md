# Keycloak Local

Run Keycloak:

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

Public client for local token testing:

```text
clinic-web
```

Service-account client used by user-service registration integration:

```text
clinic-user-service / change-me
```

Token endpoint:

```text
http://localhost:9080/realms/clinic-appointment/protocol/openid-connect/token
```

Password grant example:

```powershell
curl.exe -X POST http://localhost:9080/realms/clinic-appointment/protocol/openid-connect/token `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=password" `
  -d "client_id=clinic-web" `
  -d "username=admin@clinic.local" `
  -d "password=admin123"
```

Use the returned `access_token` against API Gateway:

```powershell
curl.exe http://localhost:8080/auth/me -H "Authorization: Bearer <access_token>"
```
