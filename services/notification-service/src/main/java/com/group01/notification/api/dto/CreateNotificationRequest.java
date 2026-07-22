package com.group01.notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    @NotNull(message = "recipientUserId cannot be null")
    private java.util.UUID recipientUserId;

    @NotBlank(message = "type cannot be blank")
    private String type;

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "body cannot be blank")
    private String body;

    @NotNull(message = "priority cannot be null")
    private Short priority;

    @NotBlank(message = "channel cannot be blank")
    private String channel;

    @NotBlank(message = "destination cannot be blank")
    private String destination;
}