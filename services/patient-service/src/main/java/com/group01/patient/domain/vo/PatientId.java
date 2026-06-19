package com.group01.patient.domain.vo;

public record PatientId(Long value) {

    public PatientId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Patient id must be positive");
        }
    }

    public static PatientId of(Long value) {
        return new PatientId(value);
    }
}
