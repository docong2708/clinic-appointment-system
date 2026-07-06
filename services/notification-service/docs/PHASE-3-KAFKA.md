# 📊 PHASE 3: Kafka Events Implementation

**Mục tiêu:** Xử lý bất đồng bộ từ các microservices khác gửi notification qua Kafka (Event-Driven).

**Thời gian:** 2-3 giờ

---

## 🎯 Luồng Hoạt Động (Inbox Pattern)

`	ext
Other Services (Appointment, Doctor) 
  → Produce Kafka Event
    → Notification Service Consumer (Kafka Listener)
      → Save to Inbox (notification_inbox_events)
        → Process inbox (Create Notification + Deliveries)
`

---

## 🔨 Các Class Cần Tạo

### 1. Kafka Consumer (Listener)
Tạo: com.group01.notification.infrastructure.event.KafkaNotificationListener
- Method: @KafkaListener(topics = "notification-events")
- Logic:
  1. Parse message (JSON)
  2. Map to NotificationInboxEvent aggregate
  3. Gọi ProcessInboxEventUseCase

### 2. DTOs for Events
- NotificationEventPayload: Định nghĩa cấu trúc JSON event từ Kafka

### 3. UseCase: ProcessInboxEvent
Tạo: com.group01.notification.application.usecase.ProcessInboxEventUseCase
- Logic:
  1. Kiểm tra deduplication (inboxEventRepository.existsBySourceEventId)
  2. Lưu vào 
otification_inbox_events (status: RECEIVED)
  3. Tạo CreateNotificationCommand
  4. Gọi CreateNotificationUseCase
  5. Update status: PROCESSED hoặc FAILED

---

## ⚙️ Configuration Setup

Đảm bảo pplication.yml hoặc 
otification-service.yaml có:
`yaml
spring:
  kafka:
    consumer:
      group-id: notification-service
`

---

## 🧪 Cách Test

1. Viết Unit Test cho Kafka Listener (dùng @EmbeddedKafka)
2. Send message JSON thủ công bằng Kafka Tool/CLI:
`json
{
  "sourceService": "appointment-service",
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "eventType": "APPOINTMENT_CREATED",
  "recipientId": "uuid...",
  "payload": { "time": "2026-07-06T10:00:00" }
}
`
3. Check bảng 
otifications xem email đã được tạo chưa
