package com.group01.notification.domain.aggregate;

import com.group01.notification.domain.vo.NotificationChannel;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class NotificationTemplate {
    private UUID id;
    private String name;
    private NotificationChannel channel;
    private String subject;
    private String body;
    private Integer version;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private NotificationTemplate() {
    }

    public static NotificationTemplate restore(
            UUID id,
            String name,
            NotificationChannel channel,
            String subject,
            String body,
            Integer version,
            Boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        NotificationTemplate template = new NotificationTemplate();
        template.id = id;
        template.name = name;
        template.channel = channel;
        template.subject = subject;
        template.body = body;
        template.version = version;
        template.active = active;
        template.createdAt = createdAt;
        template.updatedAt = updatedAt;
        return template;
    }
}
