# 🧪 Test Gửi Email Với Config Server Mới

## Bước 1: Start Config Server (Terminal 1)
```bash
mvn spring-boot:run -pl infra/config-server
```
Đợi: `Started ConfigServerApplication`

## Bước 2: Start Notification Service (Terminal 2)
```bash
mvn spring-boot:run -pl services/notification-service
```

## Bước 3: Test Gửi Email

### Option 1: Test SMTP trực tiếp (không qua event)
```powershell
Invoke-WebRequest -Uri "http://localhost:8083/test-simple-email" -Method GET
```

### Option 2: Test qua Event endpoint
```powershell
$json = @{
    sourceService = "appointment-service"
    eventId = "NEW-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
    eventType = "APPOINTMENT_CREATED"
    recipientId = "550e8400-e29b-41d4-a716-446655440002"
    aggregateId = "550e8400-e29b-41d4-a716-446655440003"
    aggregateType = "Appointment"
    payload = @{}
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8083/api/events/appointment" `
    -Method POST `
    -ContentType "application/json" `
    -Body $json
```

## Bước 4: Check Email
- Mail nhận: **phudinh193@gmail.com**
- Subject: "Test Email - Direct SMTP" (Option 1)
- Subject: "System Notification: APPOINTMENT_CREATED" (Option 2)

## Verify Config Server
Mở trình duyệt: http://localhost:8888/notification-service/default

Expected: JSON với tất cả config từ `notification-service.yml`