package com.group01.appointment.domain.vo;

import java.util.UUID;

public record PatientId(UUID value) {

    public PatientId {
        if (value == null) {
            throw new IllegalArgumentException("Patient id must not be null");
        }
    }

    public static PatientId of(UUID value) {
        return new PatientId(value);
    }
}
