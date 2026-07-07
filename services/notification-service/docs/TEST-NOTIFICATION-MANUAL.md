# 🧪 Test Notification Service - Lệnh CURL Chính Xác

## ⚠️ LƯU Ý: Dùng PowerShell hoặc CMD, không dùng Git Bash

---

## Test 1: Gửi Event REST (Phase 3)

**PowerShell:**
```powershell
$json = @{
    sourceService = "appointment-service"
    eventId = "123e4567-e89b-12d3-a456-426614174000"
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

**CMD (Command Prompt):**
```batch
curl -X POST http://localhost:8083/api/events/appointment ^
  -H "Content-Type: application/json" ^
  -d "{\"sourceService\":\"appointment-service\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\",\"eventType\":\"APPOINTMENT_CREATED\",\"recipientId\":\"550e8400-e29b-41d4-a716-446655440002\",\"aggregateId\":\"550e8400-e29b-41d4-a716-446655440003\",\"aggregateType\":\"Appointment\",\"payload\":{}}"
```

**Expected Response:**
```
HTTP/1.1 202 Accepted
```

**Check Logs:**
```
[INFO] Processing inbox event: 123e4567-e89b-12d3-a456-426614174000
[INFO] Successfully processed inbox event
```

---

## Test 2: Deduplication - Gửi Lại Cùng EventId

Chạy lại Test 1 (cùng eventId).

**Expected:**
```
[INFO] Event 123e4567-e89b-12d3-a456-426614174000 already processed, skipping
```

No new email sent.

---

## Test 3: Retry Failure → Success (Phase 4)

### Step 3.1: Giả lập lỗi SMTP
1. Stop Notification Service
2. Edit `application.yml`:
   ```yaml
   spring:
     mail:
       port: 9999  # Invalid port
   ```
3. Restart service

### Step 3.2: Gửi event mới

**PowerShell:**
```powershell
$json = @{
    sourceService = "appointment-service"
    eventId = "223e4567-e89b-12d3-a456-426614174002"
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

**Check Logs:**
```
[ERROR] Failed to send email to: ...
[INFO] Successfully processed inbox event (nhưng delivery FAILED)
```

**Check DB:**
```sql
SELECT status, retry_count, next_retry_at FROM notification_deliveries 
ORDER BY created_at DESC LIMIT 1;
-- Expected: FAILED, retry_count=1, next_retry_at = now + 5 min
```

### Step 3.3: Khôi phục và xem Retry
1. Sửa `application.yml` port về `587`
2. Restart service
3. Đợi scheduler (1 phút), nó sẽ tự động retry
4. Check Logs: `Successfully retried delivery`
5. Check DB: `status = SENT`

---

## 🗄️ SQL Commands - Kiểm Tra DB

```sql
-- Xem inbox events
SELECT * FROM notification_inbox_events ORDER BY received_at DESC LIMIT 5;

-- Xem notifications
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 5;

-- Xem deliveries + retry status
SELECT id, status, retry_count, next_retry_at, last_error 
FROM notification_deliveries ORDER BY created_at DESC LIMIT 5;

-- Xem delivery đã gửi thành công
SELECT * FROM notification_deliveries WHERE status = 'SENT' LIMIT 5;

-- Xem delivery failed
SELECT * FROM notification_deliveries WHERE status = 'FAILED' LIMIT 5;
```