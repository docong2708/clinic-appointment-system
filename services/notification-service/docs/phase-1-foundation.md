# PHASE 1: Foundation - Domain Layer & JPA Configuration

**Mục tiêu:** Làm cho notification service compile được và có thể persist vào database

**Thời gian dự kiến:** 4-6 giờ

**Trạng thái:** 🔴 CHƯA BẮT ĐẦU

---

## 📌 Tổng Quan

Phase 1 là nền tảng của toàn bộ notification service. Nếu không hoàn thành phase này, service không thể chạy được.

**Vấn đề hiện tại:**
- Code không compile vì thiếu domain classes
- JPA entities không có annotations
- Spring Data JPA bị tắt trong pom.xml

**Sau khi hoàn thành Phase 1:**
- ✅ Code compile thành công
- ✅ Service có thể start up
- ✅ Có thể lưu/đọc notifications từ database
- ✅ Domain logic hoạt động theo DDD pattern

---

## 🎯 Checklist

### 1.1 Domain Layer - Value Objects
- [ ] Tạo NotificationId
- [ ] Tạo RecipientId  
- [ ] Tạo NotificationTitle
- [ ] Tạo NotificationChannel (Enum)
- [ ] Tạo NotificationStatus (Enum)
- [ ] Tạo DeliveryStatus (Enum)
- [ ] Tạo NotificationPriority
- [ ] Tạo NotificationType

### 1.2 Domain Layer - Entities
- [ ] Tạo NotificationDelivery entity
- [ ] Tạo NotificationDeliveryAttempt entity
- [ ] Tạo NotificationPreference entity

### 1.3 Domain Layer - Aggregate Root
- [ ] Tạo NotificationAggregate
  - [ ] Business rules: validate recipient, title, priority
  - [ ] State transitions: CREATED → READY → SENT/FAILED
  - [ ] Methods: create(), addDelivery(), markAsReady(), markAsSent(), markAsFailed()

### 1.4 Domain Layer - Repository Ports
- [ ] Tạo NotificationRepository interface (port)
- [ ] Tạo NotificationTemplateRepository interface (port)

### 1.5 Application Layer - Commands
- [ ] Tạo CreateNotificationCommand
- [ ] Tạo UpdateNotificationCommand
- [ ] Tạo DeleteNotificationCommand

### 1.6 Application Layer - Ports
- [ ] Tạo NotificationSenderPort interface

### 1.7 Infrastructure - JPA Entities (Add Annotations)
- [ ] NotificationJpaEntity - thêm @Entity, @Table, @Column, relationships
- [ ] NotificationDeliveryJpaEntity - thêm annotations
- [ ] NotificationDeliveryAttemptJpaEntity - thêm annotations
- [ ] NotificationInboxEventJpaEntity - thêm annotations
- [ ] NotificationPreferenceJpaEntity - thêm annotations
- [ ] NotificationTemplateJpaEntity - thêm annotations

### 1.8 Infrastructure - Repository Adapters
- [ ] Hoàn thiện NotificationRepositoryAdapter
  - [ ] Implement save()
  - [ ] Implement findById()
  - [ ] Implement findByRecipientUserId()
- [ ] Hoàn thiện NotificationTemplateRepositoryAdapter

### 1.9 Infrastructure - Mappers
- [ ] Hoàn thiện NotificationMapper (domain ↔ JPA)
- [ ] Hoàn thiện NotificationInboxEventMapper
- [ ] Hoàn thiện NotificationTemplateMapper

### 1.10 Configuration
- [ ] Enable Spring Data JPA trong pom.xml
- [ ] Enable PostgreSQL driver trong pom.xml
- [ ] Tạo JpaConfig class với @EnableJpaRepositories
- [ ] Configure Flyway migration

### 1.11 Application - Use Cases
- [ ] Hoàn thiện ProcessNotificationUseCase (đã có code nhưng reference missing domain)
- [ ] Fix compile errors trong các use case implementations

---

## 📁 Cấu Trúc Thư Mục Cần Tạo

\\\
notification-service/
└── src/main/java/com/group01/notification/
    ├── domain/
    │   ├── aggregate/
    │   │   └── NotificationAggregate.java          ← TẠO MỚI
    │   ├── entity/
    │   │   ├── NotificationDelivery.java           ← TẠO MỚI
    │   │   ├── NotificationDeliveryAttempt.java    ← TẠO MỚI
    │   │   └── NotificationPreference.java         ← TẠO MỚI
    │   ├── vo/
    │   │   ├── NotificationId.java                 ← TẠO MỚI
    │   │   ├── RecipientId.java                    ← TẠO MỚI
    │   │   ├── NotificationTitle.java              ← TẠO MỚI
    │   │   ├── NotificationChannel.java            ← TẠO MỚI
    │   │   ├── NotificationStatus.java             ← TẠO MỚI
    │   │   ├── DeliveryStatus.java                 ← TẠO MỚI
    │   │   ├── NotificationPriority.java           ← TẠO MỚI
    │   │   └── NotificationType.java               ← TẠO MỚI
    │   └── repository/
    │       ├── NotificationRepository.java          ← TẠO MỚI
    │       └── NotificationTemplateRepository.java  ← TẠO MỚI
    ├── application/
    │   ├── command/
    │   │   ├── CreateNotificationCommand.java       ← TẠO MỚI
    │   │   ├── UpdateNotificationCommand.java       ← TẠO MỚI
    │   │   └── DeleteNotificationCommand.java       ← TẠO MỚI
    │   └── port/
    │       └── NotificationSenderPort.java          ← TẠO MỚI
    ├── infrastructure/
    │   └── persistence/
    │       ├── NotificationJpaEntity.java           ← SỬA (thêm annotations)
    │       ├── NotificationDeliveryJpaEntity.java   ← SỬA
    │       └── ... (các JPA entities khác)          ← SỬA
    └── config/
        └── JpaConfig.java                           ← TẠO MỚI
\\\

---

## 🔨 Implementation Guide

### Step 1: Value Objects (30 phút)

**1.1 NotificationId.java**
\\\java
package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class NotificationId {
    private final UUID value;

    private NotificationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationId cannot be null");
        }
        this.value = value;
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
\\\

**Tương tự cho:** RecipientId (copy pattern NotificationId)

**1.2 NotificationTitle.java**
\\\java
package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class NotificationTitle {
    private static final int MAX_LENGTH = 255;
    private final String value;

    private NotificationTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = value.trim();
    }

    public static NotificationTitle of(String value) {
        return new NotificationTitle(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
\\\

**1.3 NotificationChannel.java (Enum)**
\\\java
package com.group01.notification.domain.vo;

public enum NotificationChannel {
    IN_APP,
    EMAIL,
    SMS,
    PUSH
}
\\\

**Tương tự cho:** NotificationStatus, DeliveryStatus (copy từ schema enums)

**1.4 NotificationPriority.java**
\\\java
package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class NotificationPriority {
    private static final short MIN = 1;
    private static final short MAX = 9;
    private final short value;

    private NotificationPriority(short value) {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException("Priority must be between " + MIN + " and " + MAX);
        }
        this.value = value;
    }

    public static NotificationPriority of(short value) {
        return new NotificationPriority(value);
    }

    public static NotificationPriority normal() {
        return new NotificationPriority((short) 5);
    }

    public static NotificationPriority high() {
        return new NotificationPriority((short) 3);
    }

    public static NotificationPriority urgent() {
        return new NotificationPriority((short) 1);
    }
}
\\\

---

### Step 2: Domain Entities (45 phút)

**2.1 NotificationDelivery.java**
\\\java
package com.group01.notification.domain.entity;

import com.group01.notification.domain.vo.*;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class NotificationDelivery {
    private UUID id;
    private NotificationId notificationId;
    private NotificationChannel channel;
    private String destination;
    private DeliveryStatus status;
    private int retryCount;
    private Instant scheduledAt;
    private Instant nextRetryAt;
    private String providerName;
    private String providerMessageId;
    private String lastError;
    private Instant sentAt;
    private Instant failedAt;
    private Instant createdAt;

    private NotificationDelivery() {}

    public static NotificationDelivery create(
            NotificationId notificationId,
            NotificationChannel channel,
            String destination
    ) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be blank");
        }

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.id = UUID.randomUUID();
        delivery.notificationId = notificationId;
        delivery.channel = channel;
        delivery.destination = destination;
        delivery.status = DeliveryStatus.PENDING;
        delivery.retryCount = 0;
        delivery.createdAt = Instant.now();
        return delivery;
    }

    public void markAsSending() {
        this.status = DeliveryStatus.SENDING;
    }

    public void markAsSent(String providerMessageId) {
        this.status = DeliveryStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = Instant.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = DeliveryStatus.FAILED;
        this.lastError = errorMessage;
        this.failedAt = Instant.now();
        this.retryCount++;
    }
}
\\\

---

### Step 3: Aggregate Root (1 giờ)

**3.1 NotificationAggregate.java**
\\\java
package com.group01.notification.domain.aggregate;

import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.*;
import lombok.Getter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class NotificationAggregate {
    private NotificationId id;
    private RecipientId recipientId;
    private String type;
    private NotificationTitle title;
    private String body;
    private NotificationPriority priority;
    private NotificationStatus status;
    private List<NotificationDelivery> deliveries;
    private Instant readAt;
    private Instant createdAt;
    private Instant updatedAt;

    private NotificationAggregate() {
        this.deliveries = new ArrayList<>();
    }

    public static NotificationAggregate create(
            RecipientId recipientId,
            String type,
            NotificationTitle title,
            String body,
            NotificationPriority priority
    ) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be blank");
        }

        NotificationAggregate notification = new NotificationAggregate();
        notification.id = NotificationId.generate();
        notification.recipientId = recipientId;
        notification.type = type;
        notification.title = title;
        notification.body = body;
        notification.priority = priority;
        notification.status = NotificationStatus.CREATED;
        notification.createdAt = Instant.now();
        notification.updatedAt = Instant.now();
        return notification;
    }

    public void addDelivery(NotificationDelivery delivery) {
        this.deliveries.add(delivery);
    }

    public void markAsReady() {
        if (this.status != NotificationStatus.CREATED) {
            throw new IllegalStateException("Can only mark CREATED notifications as READY");
        }
        this.status = NotificationStatus.READY;
        this.updatedAt = Instant.now();
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.updatedAt = Instant.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void markAsRead() {
        this.readAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
\\\

---

### Step 4: Repository Ports (15 phút)

**4.1 NotificationRepository.java**
\\\java
package com.group01.notification.domain.repository;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.vo.NotificationId;
import com.group01.notification.domain.vo.RecipientId;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    NotificationAggregate save(NotificationAggregate aggregate);
    Optional<NotificationAggregate> findById(NotificationId id);
    List<NotificationAggregate> findByRecipientId(RecipientId recipientId);
    List<NotificationAggregate> findUnreadByRecipientId(RecipientId recipientId);
    void deleteById(NotificationId id);
}
\\\

---

### Step 5: Commands (20 phút)

**5.1 CreateNotificationCommand.java**
\\\java
package com.group01.notification.application.command;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class CreateNotificationCommand {
    private UUID recipientUserId;
    private String type;
    private String title;
    private String body;
    private Short priority;
    private String channel;
    private String destination;
}
\\\

---

### Step 6: Enable JPA (10 phút)

**6.1 pom.xml - Uncomment JPA dependencies**
\\\xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
\\\

**6.2 JpaConfig.java**
\\\java
package com.group01.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.group01.notification.infrastructure.persistence")
public class JpaConfig {
}
\\\

---

### Step 7: JPA Entity Annotations (1.5 giờ)

**7.1 NotificationJpaEntity.java - Thêm annotations**
\\\java
package com.group01.notification.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationJpaEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;
    
    @Column(name = "type", nullable = false, length = 100)
    private String type;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "priority", nullable = false)
    private Short priority;
    
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private String status;
    
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationDeliveryJpaEntity> deliveries = new ArrayList<>();
    
    @Column(name = "read_at")
    private Instant readAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
\\\

**7.2 NotificationDeliveryJpaEntity.java - Thêm annotations**
\\\java
package com.group01.notification.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryJpaEntity {
    
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private NotificationJpaEntity notification;
    
    @Column(name = "channel", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private String channel;
    
    @Column(name = "destination", length = 512)
    private String destination;
    
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private String status;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
    
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @Column(name = "failed_at")
    private Instant failedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
\\\

**Lặp lại pattern này cho:**
- NotificationDeliveryAttemptJpaEntity
- NotificationInboxEventJpaEntity
- NotificationPreferenceJpaEntity
- NotificationTemplateJpaEntity

---

### Step 8: Complete Repository Adapter (45 phút)

**8.1 NotificationRepositoryAdapter.java**
\\\java
package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.NotificationId;
import com.group01.notification.domain.vo.RecipientId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {
    
    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;
    
    @Override
    public NotificationAggregate save(NotificationAggregate aggregate) {
        NotificationJpaEntity entity = mapper.toJpaEntity(aggregate);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<NotificationAggregate> findById(NotificationId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public List<NotificationAggregate> findByRecipientId(RecipientId recipientId) {
        return jpaRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientId.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NotificationAggregate> findUnreadByRecipientId(RecipientId recipientId) {
        return jpaRepository.findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(recipientId.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(NotificationId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
\\\

---

### Step 9: Complete Mapper (45 phút)

**9.1 NotificationMapper.java**
\\\java
package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {
    
    public NotificationJpaEntity toJpaEntity(NotificationAggregate domain) {
        NotificationJpaEntity entity = NotificationJpaEntity.builder()
                .id(domain.getId().getValue())
                .recipientUserId(domain.getRecipientId().getValue())
                .type(domain.getType())
                .title(domain.getTitle().value())
                .body(domain.getBody())
                .priority(domain.getPriority().getValue())
                .status(domain.getStatus().name())
                .readAt(domain.getReadAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        
        // Map deliveries
        entity.setDeliveries(
            domain.getDeliveries().stream()
                .map(d -> toDeliveryJpaEntity(d, entity))
                .collect(Collectors.toList())
        );
        
        return entity;
    }
    
    public NotificationAggregate toDomain(NotificationJpaEntity entity) {
        // Use reflection or builder to reconstruct domain object
        // This is simplified - you may need to use reflection to set private fields
        NotificationAggregate aggregate = NotificationAggregate.create(
                RecipientId.of(entity.getRecipientUserId()),
                entity.getType(),
                NotificationTitle.of(entity.getTitle()),
                entity.getBody(),
                NotificationPriority.of(entity.getPriority())
        );
        
        // Map deliveries
        entity.getDeliveries().forEach(d -> {
            aggregate.addDelivery(toDeliveryDomain(d));
        });
        
        return aggregate;
    }
    
    private NotificationDeliveryJpaEntity toDeliveryJpaEntity(
            NotificationDelivery domain, 
            NotificationJpaEntity notification
    ) {
        return NotificationDeliveryJpaEntity.builder()
                .id(domain.getId())
                .notification(notification)
                .channel(domain.getChannel().name())
                .destination(domain.getDestination())
                .status(domain.getStatus().name())
                .retryCount(domain.getRetryCount())
                .providerMessageId(domain.getProviderMessageId())
                .lastError(domain.getLastError())
                .sentAt(domain.getSentAt())
                .failedAt(domain.getFailedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
    
    private NotificationDelivery toDeliveryDomain(NotificationDeliveryJpaEntity entity) {
        // Use reflection to reconstruct - simplified here
        return NotificationDelivery.create(
                NotificationId.of(entity.getNotification().getId()),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getDestination()
        );
    }
}
\\\

---

## ✅ Validation Checklist

Sau khi hoàn thành Phase 1, verify:

1. **Compile Check**
\\\ash
cd notification-service
mvn clean compile
\\\
Expected: BUILD SUCCESS

2. **Start Application**
\\\ash
mvn spring-boot:run
\\\
Expected: Application starts without errors

3. **Database Connection**
Check logs for: "HikkaりCP pool started" và "Flyway migrations applied"

4. **Repository Test**
Tạo một simple test trong NotificationServiceApplicationTests:
\\\java
@Test
void testSaveNotification() {
    NotificationAggregate notification = NotificationAggregate.create(
        RecipientId.of(UUID.randomUUID()),
        "TEST",
        NotificationTitle.of("Test Notification"),
        "Test body",
        NotificationPriority.normal()
    );
    
    NotificationAggregate saved = notificationRepository.save(notification);
    assertNotNull(saved.getId());
}
\\\

---

## 🚨 Common Issues & Solutions

### Issue 1: Compile errors in ProcessNotificationUseCase
**Solution:** Sau khi tạo domain classes, ProcessNotificationUseCase sẽ compile thành công

### Issue 2: JPA entity không map đúng với database
**Solution:** Double-check column names trong @Column annotations match với schema SQL

### Issue 3: Circular dependency khi inject repositories
**Solution:** Sử dụng constructor injection, không dùng @Autowired field injection

### Issue 4: Mapper không work vì domain objects immutable
**Solution:** Consider sử dụng reflection hoặc tạo "reconstruction" methods trong domain classes

---

## 📊 Progress Tracking

| Task | Status | Time | Notes |
|------|--------|------|-------|
| Value Objects | ⬜ | 0/30m | |
| Domain Entities | ⬜ | 0/45m | |
| Aggregate Root | ⬜ | 0/1h | |
| Repository Ports | ⬜ | 0/15m | |
| Commands | ⬜ | 0/20m | |
| Enable JPA | ⬜ | 0/10m | |
| JPA Annotations | ⬜ | 0/1.5h | |
| Repository Adapter | ⬜ | 0/45m | |
| Mapper | ⬜ | 0/45m | |
| Testing | ⬜ | 0/30m | |

**Total:** 0/6h

---

## 🎯 Definition of Done

Phase 1 được coi là hoàn thành khi:

- [ ] Tất cả domain classes được tạo và compile thành công
- [ ] Tất cả JPA entities có đầy đủ annotations
- [ ] Spring Data JPA enabled và repositories được inject thành công
- [ ] Application start up không errors
- [ ] Database migrations chạy thành công
- [ ] Có thể save và retrieve notification từ database
- [ ] ProcessNotificationUseCase compile và có thể execute (thử với mock data)
- [ ] Code coverage: domain layer >= 70%

---

## 📝 Notes

- Ưu tiên tạo value objects trước vì các classes khác depend vào chúng
- JPA entity annotations phải match chính xác với database schema (V1__create_notification_schema.sql)
- Mapper logic có thể phức tạp vì domain objects immutable - consider trade-offs
- Phase 1 không bao gồm REST API - đó là Phase 2

---

**Next:** [Phase 2 - REST API Implementation](./phase-2-rest-api.md)
