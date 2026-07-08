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
    private String sourceService;
    private UUID sourceEventId;
    private String dedupeKey;
    private String aggregateType;
    private UUID aggregateId;
    private UUID sourceInboxEventId;
}
