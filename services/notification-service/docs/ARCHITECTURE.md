# Architecture Overview

## Layers

### Domain (Business Logic)
- **Aggregates:** NotificationAggregate (main state machine)
- **Entities:** NotificationDelivery, NotificationInboxEvent, NotificationTemplate
- **Value Objects:** NotificationId, RecipientId, NotificationTitle, etc.
- **Repositories (Ports):** NotificationRepository, NotificationInboxEventRepository, NotificationTemplateRepository

### Application (Use Cases)
- CreateNotificationUseCase
- GetNotificationUseCase
- ListNotificationsUseCase
- UpdateNotificationUseCase
- DeleteNotificationUseCase

### Infrastructure (Technical Details)
- **Persistence:** JPA entities + repositories + mappers
- **Senders:** Email, Mock, In-App (via NotificationSenderPort)
- **Config:** Database, Mail, Kafka

### API (REST)
- NotificationController (endpoints)
- DTOs: CreateNotificationRequest, NotificationResponse

---

## Data Flow

\\\
REST Request → Controller → UseCase → Domain Aggregate → Repository → JPA → PostgreSQL
\\\

## Database

PostgreSQL with tables:
- notifications (main)
- notification_deliveries (multi-channel)
- notification_delivery_attempts (retry tracking)
- notification_inbox_events (event sourcing)
- notification_preferences (user settings)
- notification_templates (email templates)

---

## Patterns Used

✅ Domain-Driven Design (DDD)
✅ Hexagonal Architecture (Ports & Adapters)
✅ Event Sourcing (inbox pattern)
✅ State Machine (notification status)
