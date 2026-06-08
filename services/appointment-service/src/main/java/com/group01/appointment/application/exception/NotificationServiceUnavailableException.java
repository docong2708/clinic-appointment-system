package com.group01.appointment.application.exception;

public class NotificationServiceUnavailableException extends ExternalServiceException {

    public NotificationServiceUnavailableException(Throwable cause) {
        super("Notification service is unavailable", cause);
    }
}
