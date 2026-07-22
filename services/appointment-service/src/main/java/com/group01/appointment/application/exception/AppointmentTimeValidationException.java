package com.group01.appointment.application.exception;

import com.group01.appointment.domain.exception.DomainException;

public class AppointmentTimeValidationException extends DomainException {

    public AppointmentTimeValidationException(String message) {
        super(message);
    }
}
