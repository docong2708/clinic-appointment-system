package com.group01.appointment.application.exception;

import com.group01.appointment.domain.exception.DomainException;

public class MedicalRecordSyncException extends DomainException {

    public MedicalRecordSyncException(String message) {
        super(message);
    }

    public MedicalRecordSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
