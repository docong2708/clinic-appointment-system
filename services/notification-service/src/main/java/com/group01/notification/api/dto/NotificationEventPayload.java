package com.group01.notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventPayload {
    private String sourceService;
    private UUID eventId;
    private String eventType;
    private UUID recipientId;
    private UUID aggregateId;
    private String aggregateType;
    private LocalDateTime eventTime;
    private Object payload;
}