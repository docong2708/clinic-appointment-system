package com.group01.appointment.application.exception;

public class UserServiceUnavailableException extends ExternalServiceException {

    public UserServiceUnavailableException(Throwable cause) {
        super("Dịch vụ người dùng hiện không khả dụng", cause);
    }
}
