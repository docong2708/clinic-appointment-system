package com.group01.notification.domain.aggregate;

import com.group01.notification.domain.vo.InboxEventStatus;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationInboxEvent {
    private UUID id;
    private String sourceService;
    private UUID sourceEventId;
    private String eventType;
    private String aggregateType;
    private UUID aggregateId;
    private String payload;
    private InboxEventStatus status;
    private String correlationId;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private String errorMessage;

    private NotificationInboxEvent() {
    }

    public static NotificationInboxEvent restore(
            UUID id,
            String sourceService,
            UUID sourceEventId,
            String eventType,
            String aggregateType,
            UUID aggregateId,
            String payload,
            InboxEventStatus status,
            String correlationId,
            LocalDateTime receivedAt,
            LocalDateTime processedAt,
            String errorMessage
    ) {
        NotificationInboxEvent event = new NotificationInboxEvent();
        event.id = id;
        event.sourceService = sourceService;
        event.sourceEventId = sourceEventId;
        event.eventType = eventType;
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.payload = payload;
        event.status = status;
        event.correlationId = correlationId;
        event.receivedAt = receivedAt;
        event.processedAt = processedAt;
        event.errorMessage = errorMessage;
        return event;
    }
}
