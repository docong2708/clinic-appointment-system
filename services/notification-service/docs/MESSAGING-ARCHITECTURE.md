# Messaging Architecture - Notification Service

## Tổng Quan

Notification Service hỗ trợ **2 cách nhận events**:
1. **REST API** - Appointment Service gọi trực tiếp qua Feign Client
2. **Kafka** - Các service khác publish events vào topic Kafka

## 1. REST API Integration (Hiện Tại)

### Appointment Service → Notification Service

**Cách gọi:**
```
Appointment Service
  └─> Feign Client (NotificationServiceClient)
      └─> POST /api/events/appointment
          └─> Notification Service (EventController)
              └─> ProcessInboxEventUseCase
                  └─> Lưu Inbox Event (deduplication)
                  └─> Tạo Notification
```

**Endpoint:** \POST /api/events/appointment\

**Request Body:**
```json
{
  "sourceService": "appointment-service",
  "eventId": "uuid",
  "eventType": "APPOINTMENT_CREATED",
  "recipientId": "uuid",
  "aggregateId": "uuid",
  "aggregateType": "Appointment",
  "eventTime": "2026-07-06T10:00:00",
  "payload": {
    "appointmentId": "uuid",
    "patientId": "uuid",
    "doctorId": "uuid",
    "startTime": "2026-07-06T10:00:00",
    "endTime": "2026-07-06T11:00:00"
  }
}
```

**Config trong Appointment Service:**
```yaml
# application.yaml hoặc config server
clients:
  notification-service:
    base-url: http://localhost:8083
    appointment-events-path: /api/events/appointment
```

---

## 2. Kafka Integration (Mới)

### Các Service → Kafka → Notification Service

**Cách hoạt động:**
```
Any Service
  └─> Kafka Producer
      └─> Topic: "notification-events"
          └─> Kafka Broker
              └─> KafkaNotificationListener (Consumer)
                  └─> ProcessInboxEventUseCase
                      └─> Lưu Inbox Event (deduplication)
                      └─> Tạo Notification
```

**Topic:** \
otification-events\

**Message Format (JSON String):**
```json
{
  "sourceService": "doctor-service",
  "eventId": "uuid",
  "eventType": "DOCTOR_SCHEDULE_UPDATED",
  "recipientId": "uuid",
  "aggregateId": "uuid",
  "aggregateType": "Doctor",
  "eventTime": "2026-07-06T10:00:00",
  "payload": {
    "doctorId": "uuid",
    "scheduleDate": "2026-07-10"
  }
}
```

**Config trong Notification Service:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

**Config trong Producer Service (ví dụ: Doctor Service):**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

---

## 3. Inbox Pattern (Deduplication)

Cả REST và Kafka đều sử dụng **Inbox Pattern** để tránh xử lý trùng:

```
1. Event đến (REST hoặc Kafka)
2. Check: \inboxEventRepository.existsBySourceEventId(eventId)\
3. Nếu đã tồn tại → Skip
4. Nếu chưa → Lưu vào \
otification_inbox_events\ (status: RECEIVED)
5. Xử lý tạo Notification
6. Cập nhật status: PROCESSED hoặc FAILED
```

**Bảng Database:** \
otification_inbox_events\

| Column          | Type      | Mô tả                           |
|-----------------|-----------|---------------------------------|
| id              | UUID      | Primary key                     |
| source_service  | VARCHAR   | Service gửi event               |
| source_event_id | UUID      | ID event từ source (dedup key)  |
| event_type      | VARCHAR   | Loại event                      |
| aggregate_id    | UUID      | ID entity liên quan             |
| payload         | TEXT      | JSON payload chi tiết           |
| status          | VARCHAR   | RECEIVED/PROCESSING/PROCESSED/FAILED |
| received_at     | TIMESTAMP | Thời gian nhận                  |
| processed_at    | TIMESTAMP | Thời gian xử lý xong            |
| error_message   | TEXT      | Error (nếu FAILED)              |

---

## 4. So Sánh REST vs Kafka

| Tiêu chí              | REST API                        | Kafka                           |
|-----------------------|---------------------------------|---------------------------------|
| **Latency**           | Thấp (sync)                     | Cao hơn (async)                 |
| **Reliability**       | Phụ thuộc service availability  | High (Kafka lưu message)        |
| **Scalability**       | Bị giới hạn bởi network         | Cao (distributed)               |
| **Complexity**        | Đơn giản (HTTP)                 | Phức tạp hơn (cần Kafka setup)  |
| **Use Case**          | Notification cần real-time      | Event sourcing, audit log       |
| **Hiện tại**          | ✅ Appointment Service dùng     | 🆕 Sẵn sàng cho service khác    |

---

## 5. Cách Sử Dụng

### Cho Service Hiện Tại (Appointment Service)
**Không cần thay đổi** - Tiếp tục dùng REST qua Feign Client

### Cho Service Mới Muốn Dùng Kafka

**Bước 1:** Thêm Kafka dependency vào \pom.xml\:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Bước 2:** Config Kafka Producer:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**Bước 3:** Tạo Event Publisher:
```java
@Service
public class NotificationEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent(NotificationEventPayload payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("notification-events", message);
        } catch (Exception e) {
            log.error("Failed to publish event", e);
        }
    }
}
```

**Bước 4:** Gọi khi cần gửi notification:
```java
NotificationEventPayload payload = NotificationEventPayload.builder()
    .sourceService("doctor-service")
    .eventId(UUID.randomUUID())
    .eventType("DOCTOR_SCHEDULE_UPDATED")
    .recipientId(patientId)
    .aggregateId(doctorId)
    .aggregateType("Doctor")
    .eventTime(LocalDateTime.now())
    .payload(scheduleDetails)
    .build();

notificationEventPublisher.publishEvent(payload);
```

---

## 6. Event Types

| Service               | Event Type                    | Khi nào gửi                     |
|-----------------------|-------------------------------|---------------------------------|
| Appointment Service   | APPOINTMENT_CREATED           | Lịch hẹn được tạo               |
| Appointment Service   | APPOINTMENT_CONFIRMED         | Lịch hẹn được xác nhận          |
| Appointment Service   | APPOINTMENT_CANCELED          | Lịch hẹn bị hủy                 |
| Doctor Service        | DOCTOR_SCHEDULE_UPDATED       | Lịch bác sĩ thay đổi            |
| Patient Service       | PATIENT_REGISTERED            | Bệnh nhân đăng ký mới           |

---

## 7. Testing

### Test REST Endpoint:
```bash
curl -X POST http://localhost:8083/api/events/appointment \
  -H "Content-Type: application/json" \
  -d '{
    "sourceService": "appointment-service",
    "eventId": "123e4567-e89b-12d3-a456-426614174000",
    "eventType": "APPOINTMENT_CREATED",
    "recipientId": "uuid",
    "aggregateId": "uuid",
    "aggregateType": "Appointment",
    "payload": {}
  }'
```

### Test Kafka (với kafka-console-producer):
```bash
kafka-console-producer --broker-list localhost:9092 --topic notification-events

# Paste JSON message:
{"sourceService":"test-service","eventId":"123e4567-e89b-12d3-a456-426614174000","eventType":"TEST_EVENT","recipientId":"uuid","aggregateId":"uuid","aggregateType":"Test","payload":{}}
```

---

## 8. Monitoring

**Logs quan trọng:**
- \Processing inbox event: {eventId}\ - Bắt đầu xử lý
- \Event {} already processed, skipping\ - Dedup thành công
- \Successfully processed inbox event\ - Xử lý thành công
- \Failed to process inbox event\ - Xử lý thất bại

**Database check:**
```sql
-- Xem inbox events gần đây
SELECT * FROM notification_inbox_events 
ORDER BY received_at DESC LIMIT 10;

-- Xem failed events
SELECT * FROM notification_inbox_events 
WHERE status = 'FAILED';
```