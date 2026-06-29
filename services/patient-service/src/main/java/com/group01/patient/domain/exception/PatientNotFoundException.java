package com.group01.patient.domain.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("Patient not found with id: " + patientId);
    }
}
