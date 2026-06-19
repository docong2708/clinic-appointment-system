package com.group01.patient.domain.exception;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long patientId) {
        super("Patient not found with id: " + patientId);
    }
}
