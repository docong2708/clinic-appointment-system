# 📊 PHASE 2: REST API Implementation

**Mục tiêu:** Xây dựng đầy đủ các HTTP endpoints để tương tác với notification service.

**Thời gian:** 2-3 giờ

---

## 🎯 Các Endpoints Cần Tạo

### 1. Notification Controller (com.group01.notification.api.controller.NotificationController)

- **POST** /api/notifications
  - Body: CreateNotificationRequest
  - Logic: Gọi CreateNotificationUseCase
  - Trả về: NotificationResponse (201 Created)

- **GET** /api/notifications/{id}
  - Logic: Gọi GetNotificationUseCase
  - Trả về: NotificationResponse (200 OK)

- **GET** /api/notifications/recipient/{recipientId}
  - Logic: Gọi ListNotificationsUseCase.handleByRecipientId()
  - Trả về: List<NotificationResponse> (200 OK)

- **GET** /api/notifications/recipient/{recipientId}/unread
  - Logic: Gọi ListNotificationsUseCase.handleUnreadByRecipientId()
  - Trả về: List<NotificationResponse> (200 OK)

- **PUT** /api/notifications/{id}/read
  - Logic: Tìm notification → Gọi notification.markAsRead() → Lưu DB
  - Trả về: NotificationResponse (200 OK)

- **DELETE** /api/notifications/{id}
  - Logic: Gọi DeleteNotificationUseCase
  - Trả về: 204 No Content

---

## 🔨 Các Class Cần Hoàn Thiện

1. **NotificationController.java:** Implement đầy đủ các methods trên
2. **NotificationResponse.java:** Chắc chắn map đúng field trả về
3. **ProcessNotificationUseCase.java:** Fix lỗi compile và implement core logic tạo Notification

---

## 🧪 Cách Test

1. Sử dụng CURL hoặc Postman POST tới /api/notifications
2. Kiểm tra log báo sending mock/email
3. GET /api/notifications/recipient/{id} để check DB lưu chưa
