package com.group01.appointment.application.exception;

public class PatientServiceUnavailableException extends ExternalServiceException {

    public PatientServiceUnavailableException(Throwable cause) {
        super("Dịch vụ bệnh nhân hiện không khả dụng", cause);
    }
}
