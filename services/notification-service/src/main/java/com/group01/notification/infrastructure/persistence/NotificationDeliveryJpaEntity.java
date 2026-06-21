package com.group01.notification.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryJpaEntity {
    @Id
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "next_retry_at")
    private OffsetDateTime nextRetryAt;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (retryCount == null) {
            retryCount = 0;
        }
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}