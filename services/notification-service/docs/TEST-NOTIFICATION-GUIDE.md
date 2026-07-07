# 🧪 Hướng Dẫn Test Thủ Công - Notification Service

Tài liệu này hướng dẫn cách gọi API trực tiếp qua `curl` để kiểm thử hoạt động của Notification Service (Phase 2, 3 & 4).

---

## 🛠️ Chuẩn Bị
Đảm bảo Notification Service đang chạy tại `http://localhost:8083`.

---

## 🏁 1. Test Gửi REST Event (Phase 3)
Mô phỏng Appointment Service gửi event thông báo khi có lịch hẹn mới.

**Request:**
```bash
curl -X POST http://localhost:8083/api/events/appointment \
  -H "Content-Type: application/json" \
  -d "{\"sourceService\":\"appointment-service\",\"eventId\":\"123e4567-e89b-12d3-a456-426614174000\",\"eventType\":\"APPOINTMENT_CREATED\",\"recipientId\":\"550e8400-e29b-41d4-a716-446655440002\",\"aggregateId\":\"550e8400-e29b-41d4-a716-446655440003\",\"aggregateType\":\"Appointment\",\"payload\":{}}"
```

**Kết quả mong muốn:**
- Response code: `202 Accepted`
- Nhận được email gửi tới hòm thư `mssclinicnotify@gmail.com`
- Check Logs thấy: `Processing inbox event` -> `Email sent successfully` -> `Successfully processed inbox event`

---

## 🛡️ 2. Test Deduplication (Dùng Lại EventId - Phase 3)
Đảm bảo hệ thống không xử lý trùng lặp cùng 1 event.

**Lệnh chạy:** Chạy lại chính xác lệnh `curl` ở **Mục 1** một lần nữa.

**Kết quả mong muốn:**
- Response code: `202 Accepted`
- Check Logs thấy: `Event 123e4567-e89b-12d3-a456-426614174000 already processed, skipping`
- Không có email mới nào được gửi đi.

---

## 🔄 3. Test Tự Động Retry Khi Lỗi (Phase 4)

### Bước 3.1: Giả lập lỗi SMTP
1. Stop Notification Service.
2. Sửa `application.yml`, đổi port SMTP thành `9999` (port lỗi):
   ```yaml
   spring:
     mail:
       port: 9999
   ```
3. Khởi động lại service.

### Bước 3.2: Gửi event mới
Gửi request với `eventId` hoàn toàn mới:
```bash
curl -X POST http://localhost:8083/api/events/appointment \
  -H "Content-Type: application/json" \
  -d "{\"sourceService\":\"appointment-service\",\"eventId\":\"223e4567-e89b-12d3-a456-426614174002\",\"eventType\":\"APPOINTMENT_CREATED\",\"recipientId\":\"550e8400-e29b-41d4-a716-446655440002\",\"aggregateId\":\"550e8400-e29b-41d4-a716-446655440003\",\"aggregateType\":\"Appointment\",\"payload\":{}}"
```

**Kết quả mong muốn:**
- Kiểm tra logs sẽ thấy lỗi: `Failed to send email`
- Check DB thấy delivery ở trạng thái `FAILED`, `retry_count = 1`, `next_retry_at = [Thời gian hiện tại + 5 phút]`.
  ```sql
  SELECT status, retry_count, next_retry_at FROM notification_deliveries ORDER BY created_at DESC LIMIT 1;
  ```

### Bước 3.3: Khôi phục và xem scheduler tự động gửi lại
1. Sửa `application.yml` đổi lại port SMTP về `587`.
2. Khởi động lại service.
3. Đợi scheduler quét (mỗi phút chạy 1 lần). Khi thời gian hiện tại vượt qua `next_retry_at`, scheduler sẽ tự động gửi lại thành công.
- Check Logs thấy: `Successfully retried delivery`
- Check DB thấy status chuyển thành `SENT`, email được gửi đi thành công.

---

## 📊 4. Các Câu Lệnh SQL Tiện Dụng Để Kiểm Tra DB

Truy cập Database `notification_db` bằng DBeaver / pgAdmin / psql để xem dữ liệu thực tế:

- **Xem các event nhận được:**
  ```sql
  SELECT source_service, event_type, status, error_message FROM notification_inbox_events ORDER BY received_at DESC;
  ```
- **Xem danh sách notifications được sinh ra:**
  ```sql
  SELECT title, body, status, created_at FROM notifications ORDER BY created_at DESC;
  ```
- **Xem trạng thái gửi và lịch sử retry:**
  ```sql
  SELECT channel, destination, status, retry_count, next_retry_at, last_error FROM notification_deliveries ORDER BY created_at DESC;
  ```