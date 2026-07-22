package com.group01.appointment.application.exception;

import com.group01.appointment.domain.exception.DomainException;

public class ForbiddenAppointmentActionException extends DomainException {

    public ForbiddenAppointmentActionException(String message) {
        super(message);
    }
}
