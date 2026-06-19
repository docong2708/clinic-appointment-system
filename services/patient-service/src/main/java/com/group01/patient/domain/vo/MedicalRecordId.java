package com.group01.patient.domain.vo;

public record MedicalRecordId(Long value) {

    public MedicalRecordId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Medical record id must be positive");
        }
    }

    public static MedicalRecordId of(Long value) {
        return new MedicalRecordId(value);
    }
}

