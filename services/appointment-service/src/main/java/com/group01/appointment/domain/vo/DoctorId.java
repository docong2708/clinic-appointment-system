package com.group01.appointment.domain.vo;

import java.util.UUID;

public record DoctorId(UUID value) {

    public DoctorId {
        if (value == null) {
            throw new IllegalArgumentException("Doctor id must not be null");
        }
    }

    public static DoctorId of(UUID value) {
        return new DoctorId(value);
    }
}