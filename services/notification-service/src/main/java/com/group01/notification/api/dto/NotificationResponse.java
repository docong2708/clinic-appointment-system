package com.group01.notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID recipientUserId;
    private String type;
    private String title;
    private String body;
    private String status;
    private Short priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}