# 📊 PHASE 4: Retry Mechanism & Template System

**Mục tiêu:** Xử lý lỗi khi gửi email/SMS bằng cách retry tự động và áp dụng template.

**Thời gian:** 2-3 giờ

---

## 🎯 Retry Mechanism (Thiết Kế)

Khi gửi qua provider (Email, SMS...) thất bại:
1. Ghi nhận lỗi trong 
otification_deliveries và 
otification_delivery_attempts
2. Đặt lịch retry tiếp theo: 
ext_retry_at = now() + (retry_count * 5) minutes
3. Scheduled job chạy mỗi phút: quét các delivery ở trạng thái FAILED / PENDING có 
ext_retry_at <= now
4. Gửi lại. Nếu vượt quá max retries (3 lần), chuyển status thành FAILED vĩnh viễn.

---

## 🔨 Các Class Cần Tạo / Hoàn Thiện

### 1. Scheduler Job for Retry
Tạo: com.group01.notification.infrastructure.scheduler.NotificationRetryScheduler
- Annotation: @Scheduled(cron = "0 */1 * * * *")
- Logic:
  1. Quét DB: deliveryRepository.findDeliveriesDueForRetry()
  2. Gửi lại qua NotificationSenderPort
  3. Cập nhật status

### 2. Template System
Hoàn thiện logic trong: com.group01.notification.infrastructure.sender.NotificationTemplateService
- Logic enderTemplate():
  - Lấy template từ DB: 	emplateRepository.findByKeyAndActiveTrue(key)
  - Replace placeholders {name}, {date} bằng map variables
  - Thêm HTML format cho email đẹp mắt

---

## ⚙️ Configuration Setup

Thêm @EnableScheduling trong NotificationServiceApplication.java.

---

## 🧪 Cách Test

1. Tắt mạng / config sai SMTP port để email send fail
2. Tạo notification → Delivery status: FAILED
3. Check 
ext_retry_at tăng dần
4. Bật lại SMTP, đợi scheduled job chạy tự động gửi thành công
