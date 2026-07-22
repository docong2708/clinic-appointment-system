package com.group01.notification.domain.vo;

import lombok.Value;

/**
 * Value Object representing an email type.
 */
@Value
public class EmailType {
    String value;

    public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    public static final String PURCHASE_CONFIRMATION = "PURCHASE_CONFIRMATION";
    public static final String LICENSE_DELIVERY = "LICENSE_DELIVERY";
    public static final String APP_APPROVED = "APP_APPROVED";
    public static final String APP_REJECTED = "APP_REJECTED";
    public static final String APPOINTMENT_CREATED = "APPOINTMENT_CREATED";
    public static final String APPOINTMENT_CONFIRMED = "APPOINTMENT_CONFIRMED";
    public static final String APPOINTMENT_CANCELED = "APPOINTMENT_CANCELED";
    public static final String APPOINTMENT_UPDATED = "APPOINTMENT_UPDATED";

    public EmailType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email type cannot be empty");
        }
        this.value = value;
    }

    public static boolean isValidEmailType(String type) {
        return type != null && (
            type.equals(EMAIL_VERIFICATION) ||
            type.equals(RESET_PASSWORD) ||
            type.equals(PURCHASE_CONFIRMATION) ||
            type.equals(LICENSE_DELIVERY) ||
            type.equals(APP_APPROVED) ||
            type.equals(APP_REJECTED) ||
            type.equals(APPOINTMENT_CREATED) ||
            type.equals(APPOINTMENT_CONFIRMED) ||
            type.equals(APPOINTMENT_CANCELED) ||
            type.equals(APPOINTMENT_UPDATED)
        );
    }

    public String getTemplateKey() {
        return switch (value) {
            case EMAIL_VERIFICATION -> "email-verification";
            case RESET_PASSWORD -> "reset-password";
            case PURCHASE_CONFIRMATION -> "purchase-confirmation";
            case LICENSE_DELIVERY -> "license-delivery";
            case APP_APPROVED -> "app-approved";
            case APP_REJECTED -> "app-rejected";
            case APPOINTMENT_CREATED -> "appointment-created";
            case APPOINTMENT_CONFIRMED -> "appointment-confirmed";
            case APPOINTMENT_CANCELED -> "appointment-canceled";
            case APPOINTMENT_UPDATED -> "appointment-updated";
            default -> null;
        };
    }
}
