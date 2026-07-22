package com.group01.notification.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for sending email notifications.
 * Used by business services to trigger email sending via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailRequest {

    /**
     * Unique event identifier (UUID)
     */
    @JsonProperty("eventId")
    private String eventId;

    /**
     * Email type: EMAIL_VERIFICATION, RESET_PASSWORD, PURCHASE_CONFIRMATION,
     * LICENSE_DELIVERY, APP_APPROVED, APP_REJECTED, APPOINTMENT_CREATED,
     * APPOINTMENT_CONFIRMED, APPOINTMENT_CANCELED, APPOINTMENT_UPDATED
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * Recipient's email address
     */
    @JsonProperty("recipientEmail")
    private String recipientEmail;

    /**
     * Recipient's display name
     */
    @JsonProperty("recipientName")
    private String recipientName;

    /**
     * User ID of recipient for tracking purposes
     */
    @JsonProperty("recipientUserId")
    private String recipientUserId;

    /**
     * Dynamic data for template rendering.
     * Example: {"verificationLink": "https://...", "userName": "John"}
     */
    @JsonProperty("payload")
    private Map<String, Object> payload;
}
