package com.group01.appointment.application.exception;

public class NotificationServiceUnavailableException extends ExternalServiceException {

    public NotificationServiceUnavailableException(Throwable cause) {
        super("Dịch vụ thông báo hiện không khả dụng", cause);
    }
}
