package com.group01.notification.domain.aggregate;

import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.*;
import lombok.Getter;
import java.time.LocalDateTime;
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
    private NotificationStatus status;
    private Short priority;
    private String locale;
    private UUID sourceEventId;
    private String sourceService;
    private String dedupeKey;
    private String aggregateType;
    private UUID aggregateId;
    private UUID sourceInboxEventId;
    private String templateKey;
    private Integer templateVersion;
    private String actionUrl;
    private List<NotificationDelivery> deliveries;
    private LocalDateTime readAt;
    private LocalDateTime archivedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NotificationAggregate() {
        this.deliveries = new ArrayList<>();
    }

    public static NotificationAggregate create(
            RecipientId recipientId,
            String type,
            NotificationTitle title,
            String body,
            Short priority,
            String sourceService,
            UUID sourceEventId,
            String dedupeKey,
            String aggregateType,
            UUID aggregateId,
            UUID sourceInboxEventId
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
        notification.locale = "vi-VN";
        notification.sourceService = sourceService;
        notification.sourceEventId = sourceEventId;
        notification.dedupeKey = dedupeKey;
        notification.aggregateType = aggregateType;
        notification.aggregateId = aggregateId;
        notification.sourceInboxEventId = sourceInboxEventId;
        notification.createdAt = LocalDateTime.now();
        notification.updatedAt = LocalDateTime.now();
        return notification;
    }

    public static NotificationAggregate restore(
            NotificationId id,
            RecipientId recipientId,
            String type,
            NotificationTitle title,
            String body,
            NotificationStatus status,
            Short priority,
            String locale,
            UUID sourceEventId,
            String sourceService,
            String dedupeKey,
            String aggregateType,
            UUID aggregateId,
            UUID sourceInboxEventId,
            String templateKey,
            Integer templateVersion,
            String actionUrl,
            LocalDateTime readAt,
            LocalDateTime archivedAt,
            LocalDateTime expiresAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        NotificationAggregate notification = new NotificationAggregate();
        notification.id = id;
        notification.recipientId = recipientId;
        notification.type = type;
        notification.title = title;
        notification.body = body;
        notification.status = status;
        notification.priority = priority;
        notification.locale = locale;
        notification.sourceEventId = sourceEventId;
        notification.sourceService = sourceService;
        notification.dedupeKey = dedupeKey;
        notification.aggregateType = aggregateType;
        notification.aggregateId = aggregateId;
        notification.sourceInboxEventId = sourceInboxEventId;
        notification.templateKey = templateKey;
        notification.templateVersion = templateVersion;
        notification.actionUrl = actionUrl;
        notification.readAt = readAt;
        notification.archivedAt = archivedAt;
        notification.expiresAt = expiresAt;
        notification.createdAt = createdAt;
        notification.updatedAt = updatedAt;
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
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSending() {
        this.status = NotificationStatus.PARTIALLY_SENT;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(NotificationTitle title, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be blank");
        }
        this.title = title;
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.archivedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
