package com.group01.appointment.application.exception;

public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
