package com.group01.appointment.application.exception;

import com.group01.appointment.domain.exception.DomainException;

public class DoctorCancellationNotAllowedException extends DomainException {

    public DoctorCancellationNotAllowedException(String message) {
        super(message);
    }
}
