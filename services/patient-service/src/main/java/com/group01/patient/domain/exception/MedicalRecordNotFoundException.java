package com.group01.patient.domain.exception;

public class MedicalRecordNotFoundException extends RuntimeException {

    public MedicalRecordNotFoundException(Long id) {
        super("Medical record not found with id: " + id);
    }
}
