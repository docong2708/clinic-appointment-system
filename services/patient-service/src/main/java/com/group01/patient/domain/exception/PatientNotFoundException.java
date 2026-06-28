package com.group01.patient.domain.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long patientId) {
        super("Patient not found with id: " + patientId);
    }

    public PatientNotFoundException(UUID userId) {
        super("Patient not found with user id: " + userId);
    }
}
