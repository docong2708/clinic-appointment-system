package com.group01.notification.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email sending response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailResponse {

    @JsonProperty("notificationId")
    private String notificationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;
}
