package com.group01.doctor.domain.exception;

public class BadRequestException extends DomainException {

    public BadRequestException(String message) {
        super(message);
    }
}
