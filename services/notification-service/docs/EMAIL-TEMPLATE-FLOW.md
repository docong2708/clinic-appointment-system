# 📧 Email Template Flow - Hướng Dẫn Dễ Hiểu

**Version:** 1.0  
**Date:** 2026-07-09  
**Status:** Production Ready  

---

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Luồng Hoạt Động](#luồng-hoạt-động)
3. [Template System](#template-system)
4. [Ví Dụ Chi Tiết](#ví-dụ-chi-tiết)
5. [Cách Sử Dụng](#cách-sử-dụng)

---

## Tổng Quan

Email System sử dụng **template-based approach** để gửi email:
- Templates được lưu trong **database**
- Sử dụng **{{placeholder}}** syntax để thay thế dữ liệu động
- Hỗ trợ **6 loại email** khác nhau
- Có cả **sync** và **async** (RabbitMQ) mode

---

## Luồng Hoạt Động

### Bước 1: Business Service Gửi Request

```java
// User Service muốn gửi email verification
POST /api/v1/internal/emails/send
{
  "eventType": "EMAIL_VERIFICATION",
  "recipientEmail": "user@example.com",
  "recipientName": "Nguyễn Văn A",
  "payload": {
    "userName": "Nguyễn Văn A",
    "verificationLink": "https://app.com/verify?token=abc123"
  }
}
```

### Bước 2: EmailController Nhận Request

```
EmailController
    ↓
Validate request
    ↓
Gọi SendEmailUseCase
```

### Bước 3: Load Template Từ Database

```java
// System tìm template dựa trên eventType
eventType: "EMAIL_VERIFICATION"
    ↓
templateKey: "email-verification"
    ↓
Load từ database: notification_templates table
```

**Template trong DB:**
```
Subject: Verify your email address
Body: Hi {{userName}},

Thank you for registering. Please click the link below:
{{verificationLink}}
```

### Bước 4: Render Template với Placeholders

```
EmailTemplateRenderer nhận:
- Template subject: "Verify your email address"
- Template body: "Hi {{userName}},..."
- Payload: {"userName": "Nguyễn Văn A", "verificationLink": "https://..."}

Quá trình render:
1. Tìm tất cả {{placeholder}} trong template
2. Thay thế bằng giá trị từ payload
3. Trả về text đã render
```

**Kết quả sau render:**
```
Subject: Verify your email address
Body: Hi Nguyễn Văn A,

Thank you for registering. Please click the link below:
https://app.com/verify?token=abc123
```

### Bước 5: Gửi Email Qua SMTP

```
EmailSenderService
    ↓
JavaMailSender
    ↓
SMTP Server (Gmail, SendGrid, etc.)
    ↓
Recipient's inbox
```

### Bước 6: Trả Response

```json
{
  "notificationId": "email_1234567890",
  "status": "SENT",
  "message": "Email queued for delivery"
}
```

---

## Template System

### Template Structure

Mỗi template trong database có:
- **name**: Template key (VD: "email-verification")
- **channel**: "EMAIL"
- **subject**: Tiêu đề email (có thể có placeholders)
- **body**: Nội dung email (có thể có placeholders)
- **version**: Version của template
- **active**: Template có active không

### Placeholder Syntax

```
{{placeholderName}}
```

**Ví dụ:**
```
Hi {{userName}},
Your order {{orderId}} has been confirmed.
Total: {{totalAmount}}
```

### 6 Email Templates Trong System

| # | Event Type | Template Key | Placeholders |
|---|------------|--------------|--------------|
| 1 | EMAIL_VERIFICATION | email-verification | userName, verificationLink |
| 2 | RESET_PASSWORD | reset-password | userName, resetLink |
| 3 | PURCHASE_CONFIRMATION | purchase-confirmation | userName, orderId, productName, totalAmount, orderDate |
| 4 | LICENSE_DELIVERY | license-delivery | userName, productName, licenseKey |
| 5 | APP_APPROVED | app-approved | userName, appName, appLink |
| 6 | APP_REJECTED | app-rejected | userName, appName, reason |

---

## Ví Dụ Chi Tiết

### Ví Dụ 1: Email Verification

**1. Request từ User Service:**
```json
{
  "eventType": "EMAIL_VERIFICATION",
  "recipientEmail": "john@example.com",
  "recipientName": "John Doe",
  "payload": {
    "userName": "John Doe",
    "verificationLink": "https://app.com/verify?token=xyz789"
  }
}
```

**2. Template từ Database:**
```
Subject: Verify your email address
Body: Hi {{userName}},

Thank you for registering. Please click the link below to verify your email address:
{{verificationLink}}

If you did not create an account, no further action is required.
```

**3. Email Sau Render:**
```
Subject: Verify your email address
Body: Hi John Doe,

Thank you for registering. Please click the link below to verify your email address:
https://app.com/verify?token=xyz789

If you did not create an account, no further action is required.
```

**4. Email được gửi đến:** john@example.com

---

### Ví Dụ 2: Purchase Confirmation

**1. Request từ Payment Service:**
```json
{
  "eventType": "PURCHASE_CONFIRMATION",
  "recipientEmail": "buyer@example.com",
  "recipientName": "Jane Smith",
  "payload": {
    "userName": "Jane Smith",
    "orderId": "ORD-20260709-001",
    "productName": "Premium License",
    "totalAmount": "$99.00",
    "orderDate": "2026-07-09"
  }
}
```

**2. Template từ Database:**
```
Subject: Order Confirmation - {{orderId}}
Body: Hi {{userName}},

Thank you for your purchase! Your order has been confirmed.

Order Details:
- Order ID: {{orderId}}
- Product: {{productName}}
- Amount: {{totalAmount}}
- Date: {{orderDate}}

We will send you a separate email with your license key shortly.
```

**3. Email Sau Render:**
```
Subject: Order Confirmation - ORD-20260709-001
Body: Hi Jane Smith,

Thank you for your purchase! Your order has been confirmed.

Order Details:
- Order ID: ORD-20260709-001
- Product: Premium License
- Amount: $99.00
- Date: 2026-07-09

We will send you a separate email with your license key shortly.
```

**4. Email được gửi đến:** buyer@example.com

---

## Cách Sử Dụng

### API Endpoints

#### 1. Sync Email (Trực tiếp)

```bash
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "EMAIL_VERIFICATION",
    "recipientEmail": "user@example.com",
    "recipientName": "User Name",
    "payload": {
      "userName": "User Name",
      "verificationLink": "https://app.com/verify?token=abc"
    }
  }'
```

#### 2. Async Email (Qua RabbitMQ)

```bash
curl -X POST http://localhost:8083/api/v1/internal/emails/async/send \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "PURCHASE_CONFIRMATION",
    "recipientEmail": "buyer@example.com",
    "recipientName": "Buyer Name",
    "payload": {
      "userName": "Buyer Name",
      "orderId": "ORD-123",
      "productName": "Product X",
      "totalAmount": "$50.00",
      "orderDate": "2026-07-08"
    }
  }'
```

### Các Event Type Hỗ Trợ

```java
// Sử dụng constants từ EmailType.java
EmailType.EMAIL_VERIFICATION      → email-verification
EmailType.RESET_PASSWORD          → reset-password
EmailType.PURCHASE_CONFIRMATION   → purchase-confirmation
EmailType.LICENSE_DELIVERY        → license-delivery
EmailType.APP_APPROVED            → app-approved
EmailType.APP_REJECTED            → app-rejected
```

### Template Placeholders (Bắt buộc)

**Email Verification:**
- `userName`: Tên người dùng
- `verificationLink`: Link xác thực

**Reset Password:**
- `userName`: Tên người dùng
- `resetLink`: Link reset password

**Purchase Confirmation:**
- `userName`: Tên người mua
- `orderId`: Mã đơn hàng
- `productName`: Tên sản phẩm
- `totalAmount`: Tổng tiền
- `orderDate`: Ngày đặt hàng

**License Delivery:**
- `userName`: Tên người dùng
- `productName`: Tên sản phẩm
- `licenseKey`: Mã license

**App Approved:**
- `userName`: Tên developer
- `appName`: Tên ứng dụng
- `appLink`: Link ứng dụng

**App Rejected:**
- `userName`: Tên developer
- `appName`: Tên ứng dụng
- `reason`: Lý do từ chối

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                 Business Service                         │
│  (User/Payment/App Service)                              │
├─────────────────────────────────────────────────────────┤
│ 1. Tạo event (Registration/Purchase/App Review)          │
│ 2. Gọi Notification Service                              │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│              Notification Service                        │
│                                                          │
├───────────────────┬─────────────────────────────────────┤
│ 3. Nhận request từ API                                  │
│ 4. Validate event type                                  │
│ 5. Lấy template key từ event type                       │
│ 6. Load template từ database                            │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│             EmailTemplateRenderer                        │
│                                                          │
├───────────────────┬─────────────────────────────────────┤
│ 7. Tìm {{placeholders}} trong template                  │
│ 8. Thay thế bằng values từ payload                      │
│ 9. Trả về rendered email                                │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│              EmailSenderService                          │
│                                                          │
├───────────────────┬─────────────────────────────────────┤
│ 10. Kết nối SMTP (Gmail/SendGrid)                      │
│ 11. Gửi email                                           │
│ 12. Return message id                                   │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│                    Email Delivered                       │
│                    to Recipient                         │
└─────────────────────────────────────────────────────────┘
```

---

## Lưu Ý Quan Trọng

### 1. Template Management
- Templates được lưu trong table `notification_templates`
- Có thể update templates mà không cần deploy code
- Có version control cho templates

### 2. Error Handling
- Nếu placeholder bị missing → sử dụng empty string
- Nếu template không tồn tại → return 404 error
- Nếu SMTP lỗi → log error và retry

### 3. Configuration
```yaml
# application-mail.yml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### 4. Database Schema
```sql
-- Templates được lưu trong notification_templates
INSERT INTO notification_templates 
(name, channel, subject, body, version, active)
VALUES 
('email-verification', 'EMAIL', 'Verify your email', 
 'Hi {{userName}}, ...', 1, TRUE);
```

---

## Quick Start

1. **Check service đang chạy:**
```bash
curl http://localhost:8083/api/v1/internal/emails/health
```

2. **Test email verification:**
```bash
# Gửi test email
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{ "eventType": "EMAIL_VERIFICATION", ... }'
```

3. **Verify template rendering:**
```bash
# Kiểm tra logs
tail -f notification-service.log | grep -i "template\|render\|email"
```

---

**Tóm tắt:**
- ✅ Template-driven system
- ✅ 6 email types
- ✅ {{placeholder}} syntax
- ✅ Database-backed templates
- ✅ Sync & async mode
- ✅ Easy to understand flow

**Status:** ✅ Production Ready
**Last Updated:** 2026-07-09
**Author:** Engineering Team
