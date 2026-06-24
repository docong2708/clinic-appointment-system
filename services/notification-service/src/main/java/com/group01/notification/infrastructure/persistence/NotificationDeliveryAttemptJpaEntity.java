package com.group01.notification.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDeliveryAttemptJpaEntity {
    @Id
    private UUID id;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false)
    private String status;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempted_at", nullable = false)
    private OffsetDateTime attemptedAt;

    @PrePersist
    protected void onCreate() {
        attemptedAt = OffsetDateTime.now();
    }
}