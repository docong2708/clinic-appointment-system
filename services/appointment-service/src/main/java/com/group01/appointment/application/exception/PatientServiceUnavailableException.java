package com.group01.appointment.application.exception;

public class PatientServiceUnavailableException extends ExternalServiceException {

    public PatientServiceUnavailableException(Throwable cause) {
        super("Patient service is unavailable", cause);
    }
}
