# Phase 4: Retry Mechanism & Template System - Complete

## Tóm Tắt Phase 4

Đã implement retry mechanism tự động cho failed deliveries kèm HTML email template system.

## Files Đã Cập Nhật/Tạo

**Domain Layer:**
- NotificationDelivery.java - Cập nhật markAsFailed() để set 
extRetryAt dựa vào retry count
  - Retry schedule: 
ow() + (retryCount * 5) minutes
  - Max retries: 3 lần
  - Nếu vượt 3 lần: 
extRetryAt = null (không retry nữa)

**Infrastructure Layer:**
- NotificationDeliveryJpaRepository.java - Thêm query indDeliveriesDueForRetry()
  - Query: Tìm delivery với status FAILED/PENDING, retryCount < 3, nextRetryAt <= now
- NotificationRetryScheduler.java - **Tạo mới** (97 lines)
  - Schedule: Chạy mỗi 1 phút (@Scheduled(cron = "0 */1 * * * *"))
  - Logic: Quét DB, gửi lại delivery, cập nhật status
- EmailSenderService.java - Cập nhật để hỗ trợ HTML email
  - Dùng MimeMessage + MimeMessageHelper thay vì SimpleMailMessage
  - Auto-detect HTML format từ body content
- NotificationTemplateService.java - Thêm wrapInHtmlTemplate() method
  - Wrap plain text vào HTML structure đẹp mắt
  - Style: Responsive, professional design

**Configuration:**
- NotificationServiceApplication.java - Thêm @EnableScheduling annotation

## Retry Flow

`
Delivery fail (status = FAILED)
  ├─ Set retryCount++
  ├─ Set nextRetryAt = now() + (retryCount * 5) min
  └─ Lưu DB

Scheduled Job chạy mỗi 1 phút
  ├─ Query: findDeliveriesDueForRetry(now)
  ├─ Lặp qua từng delivery
  ├─ Gửi lại qua NotificationSenderPort
  └─ Cập nhật status: SENT hoặc FAILED (tăng retryCount)

Khi retryCount >= 3
  └─ Set nextRetryAt = null (không schedule retry nữa)
`

## HTML Email Template

Tất cả email sử dụng HTML format:
`html
<!DOCTYPE html>
<html>
<head>
  <style>body { font-family: Arial, sans-serif; color: #333; }</style>
</head>
<body>
  <div style='max-width: 600px; padding: 20px; border: 1px solid #ddd;'>
    <h2>{Title}</h2>
    <div>{Content}</div>
    <hr>
    <p style='font-size: 12px; color: #7f8c8d;'>Automated message from Clinic System</p>
  </div>
</body>
</html>
`

## Testing Phase 4

### Test 1: Manual Retry Test
`ash
1. Create notification → delivery fails (SMTP error)
2. Check DB: notification_deliveries
   - status = FAILED
   - retry_count = 1
   - next_retry_at = now() + 5 min
3. Wait scheduler runs (or trigger manually)
4. Check logs: "[RETRY] Successfully retried delivery"
5. Check DB: status updated
`

### Test 2: Max Retries Test
`ash
1. Create notification
2. Force 3 failed attempts
3. Check: next_retry_at = NULL (no more retries)
4. Verify: status = FAILED (permanent)
`

### Test 3: HTML Email Rendering
`ash
1. Create notification with HTML body
2. Send email
3. Verify: Email received with HTML formatting
4. Check: Responsive design works on mobile
`

## Configuration để Test

Trong pplication.yml, để test retry (disable actual SMTP):
`yaml
spring:
  mail:
    host: localhost  # Invalid SMTP server để test failure
    port: 9999       # Port không tồn tại
`

Sau đó enable khi muốn send thực tế:
`yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: noreply@clinic.com
    password: 
`

## Database Schema

Bảng 
otification_deliveries có các trường:
- id - UUID, primary key
- status - PENDING, SENDING, SENT, FAILED, CANCELED
- etry_count - Số lần đã retry (max 3)
- 
ext_retry_at - Thời gian retry tiếp theo (NULL nếu không retry nữa)
- last_error - Message lỗi từ lần gửi cuối cùng
- provider_message_id - ID từ email provider (nếu sent)
- sent_at, ailed_at - Timestamps

## Monitoring & Debugging

**Logs quan trọng:**
- [SCHEDULER] Found X deliveries to retry - Job bắt đầu
- [RETRY] Successfully retried delivery {id} - Retry thành công
- [RETRY] Failed to retry delivery {id} - Retry thất bại

**SQL Queries để debug:**
`sql
-- Xem delivery cần retry
SELECT * FROM notification_deliveries 
WHERE status IN ('FAILED', 'PENDING') 
AND retry_count < 3 
AND (next_retry_at <= NOW() OR next_retry_at IS NULL)
ORDER BY next_retry_at ASC;

-- Xem delivery đã fail vĩnh viễn
SELECT * FROM notification_deliveries 
WHERE status = 'FAILED' AND retry_count >= 3;

-- Xem delivery thành công
SELECT * FROM notification_deliveries 
WHERE status = 'SENT' 
ORDER BY sent_at DESC LIMIT 10;
`

## Next Steps (Optional)

- Thêm Dead Letter Queue (DLQ) cho deliveries fail vĩnh viễn
- Implement exponential backoff (thay vì linear 5 min * retry_count)
- Thêm webhook notification cho failed deliveries
- Create admin dashboard để monitor retry status
- Implement SMS channel với retry logic tương tự

---

**Build Status:** ✅ Compile successful - Phase 4 ready for production