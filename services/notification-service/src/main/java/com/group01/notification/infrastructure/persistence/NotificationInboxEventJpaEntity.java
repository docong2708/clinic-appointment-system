package com.group01.notification.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_inbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInboxEventJpaEntity {
    @Id
    private UUID id;

    @Column(name = "source_service", nullable = false)
    private String sourceService;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "received_at", nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        receivedAt = OffsetDateTime.now();
    }
}