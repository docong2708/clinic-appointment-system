package com.group01.appointment.application.exception;

import com.group01.appointment.domain.exception.DomainException;

public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(message);
    }
}
