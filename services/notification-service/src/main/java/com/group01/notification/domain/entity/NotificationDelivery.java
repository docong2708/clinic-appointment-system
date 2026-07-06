package com.group01.notification.domain.entity;

import com.group01.notification.domain.vo.*;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationDelivery {
    private DeliveryId id;
    private NotificationId notificationId;
    private NotificationChannel channel;
    private String destination;
    private DeliveryStatus status;
    private Integer retryCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime nextRetryAt;
    private String providerName;
    private String providerMessageId;
    private String lastError;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NotificationDelivery() {
    }

    public static NotificationDelivery create(
            NotificationId notificationId,
            NotificationChannel channel,
            String destination
    ) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination cannot be blank");
        }

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.id = DeliveryId.generate();
        delivery.notificationId = notificationId;
        delivery.channel = channel;
        delivery.destination = destination;
        delivery.status = DeliveryStatus.PENDING;
        delivery.retryCount = 0;
        delivery.createdAt = LocalDateTime.now();
        delivery.updatedAt = LocalDateTime.now();
        return delivery;
    }

    public static NotificationDelivery restore(
            DeliveryId id,
            NotificationId notificationId,
            NotificationChannel channel,
            String destination,
            DeliveryStatus status,
            Integer retryCount,
            LocalDateTime scheduledAt,
            LocalDateTime nextRetryAt,
            String providerName,
            String providerMessageId,
            String lastError,
            LocalDateTime sentAt,
            LocalDateTime failedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.id = id;
        delivery.notificationId = notificationId;
        delivery.channel = channel;
        delivery.destination = destination;
        delivery.status = status;
        delivery.retryCount = retryCount;
        delivery.scheduledAt = scheduledAt;
        delivery.nextRetryAt = nextRetryAt;
        delivery.providerName = providerName;
        delivery.providerMessageId = providerMessageId;
        delivery.lastError = lastError;
        delivery.sentAt = sentAt;
        delivery.failedAt = failedAt;
        delivery.createdAt = createdAt;
        delivery.updatedAt = updatedAt;
        return delivery;
    }

    public void markAsSending() {
        this.status = DeliveryStatus.SENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSent(String providerMessageId) {
        this.status = DeliveryStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = DeliveryStatus.FAILED;
        this.lastError = errorMessage;
        this.failedAt = LocalDateTime.now();
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
