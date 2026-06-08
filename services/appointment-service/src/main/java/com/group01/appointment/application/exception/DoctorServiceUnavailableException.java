package com.group01.appointment.application.exception;

public class DoctorServiceUnavailableException extends ExternalServiceException {

    public DoctorServiceUnavailableException(Throwable cause) {
        super("Doctor service is unavailable", cause);
    }
}
