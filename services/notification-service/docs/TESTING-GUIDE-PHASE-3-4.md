# Testing Guide - Phase 3 & 4

## Prerequisites
- PostgreSQL running (localhost:5432)
- Notification service compiled successfully
- Gmail configured: mssclinicnotify@gmail.com

---

## Phase 3: Event Integration Tests

### Test 3.1: REST API Event

Start service:
```bash
cd clinic-appointment-system/services/notification-service
mvn spring-boot:run
```

Send REST event:
```bash
curl -X POST http://localhost:8083/api/events/appointment \
  -H "Content-Type: application/json" \
  -d "{\"sourceService\":\"appointment-service\",\"eventId\":\"550e8400-e29b-41d4-a716-446655440001\",\"eventType\":\"APPOINTMENT_CREATED\",\"recipientId\":\"550e8400-e29b-41d4-a716-446655440002\",\"aggregateId\":\"550e8400-e29b-41d4-a716-446655440003\",\"aggregateType\":\"Appointment\",\"payload\":{}}"
```

Expected: HTTP 202 Accepted

Verify DB:
```sql
SELECT * FROM notification_inbox_events ORDER BY received_at DESC LIMIT 1;
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 1;
SELECT * FROM notification_deliveries ORDER BY created_at DESC LIMIT 1;
```

### Test 3.2: Deduplication

Send same event again (same eventId).

Expected log: "Event already processed, skipping"

No new notification created.

---

## Phase 4: Retry & Email Tests

### Test 4.1: Email Success

Send event (Test 3.1).

Check logs: "Email sent successfully"

Check Gmail inbox: mssclinicnotify@gmail.com

Verify DB:
```sql
SELECT status, sent_at FROM notification_deliveries 
ORDER BY created_at DESC LIMIT 1;
-- status = 'SENT'
```

### Test 4.2: Retry on Failure

Stop service, update application.yml:
```yaml
spring.mail.port: 9999  # Invalid port
```

Restart, send event.

Check DB:
```sql
SELECT status, retry_count, next_retry_at 
FROM notification_deliveries 
ORDER BY created_at DESC LIMIT 1;
-- status = 'FAILED', retry_count = 1
```

Wait for scheduler (1 minute), check retry_count increases.

### Test 4.3: Fix and Retry Success

Update application.yml back to port 587.

Restart service.

Scheduler auto-retries, delivery becomes SENT.

### Test 4.4: Max Retries

Force 3 failures.

Check DB:
```sql
SELECT retry_count, next_retry_at 
FROM notification_deliveries ...
-- retry_count = 3, next_retry_at = NULL
```

Scheduler stops retrying.

---

## Quick Test Checklist

Phase 3:
- [ ] REST event creates notification
- [ ] Deduplication works
- [ ] Inbox event saved to DB

Phase 4:
- [ ] Email sends successfully
- [ ] Failed delivery retries
- [ ] Max 3 retries respected
- [ ] Scheduler runs every minute
- [ ] HTML email renders

---

## Troubleshooting

**Email fails:**
- Verify Gmail App Password
- Check internet connection

**Scheduler not running:**
- Check @EnableScheduling annotation
- Check logs for "Starting retry scheduler"

**DB errors:**
- Ensure PostgreSQL running
- Check schema created (notification_db)