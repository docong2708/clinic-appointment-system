package com.group01.doctor.domain.valueobject;

import java.util.UUID;

public record DoctorLeaveId(UUID value) {

    public DoctorLeaveId {
        if (value == null) {
            throw new IllegalArgumentException("Doctor leave id cannot be null");
        }
    }

    public static DoctorLeaveId generate() {
        return new DoctorLeaveId(UUID.randomUUID());
    }

    public static DoctorLeaveId of(UUID value) {
        return new DoctorLeaveId(value);
    }
}
