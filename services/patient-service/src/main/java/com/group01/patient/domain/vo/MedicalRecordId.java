package com.group01.patient.domain.vo;

import java.util.UUID;

public record MedicalRecordId(UUID value) {

    public MedicalRecordId {
        if (value == null) {
            throw new IllegalArgumentException("Medical record id must not be null");
        }
    }

    public static MedicalRecordId of(UUID value) {
        return new MedicalRecordId(value);
    }
}

