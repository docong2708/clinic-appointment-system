package com.group01.doctor.domain.valueobject;

import java.util.UUID;

public record DoctorId(UUID value) {

    public DoctorId {
        if (value == null) {
            throw new IllegalArgumentException("Doctor ID must not be null");
        }
    }

    public static DoctorId of(UUID value) {
        return new DoctorId(value);
    }

    public static DoctorId generate() {
        return new DoctorId(UUID.randomUUID());
    }
}
