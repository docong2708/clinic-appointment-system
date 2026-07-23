package com.group01.patient.domain.vo;

import java.util.UUID;

public record MedicalRecordId(UUID value) {

    public MedicalRecordId {
        if (value == null) {
            throw new IllegalArgumentException("Mã hồ sơ khám không được để trống");
        }
    }

    public static MedicalRecordId of(UUID value) {
        return new MedicalRecordId(value);
    }
}

