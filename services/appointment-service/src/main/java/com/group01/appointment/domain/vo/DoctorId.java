package com.group01.appointment.domain.vo;

import java.util.UUID;

public record DoctorId(UUID value) {

    public DoctorId {
        if (value == null) {
            throw new IllegalArgumentException("Mã bác sĩ không được để trống");
        }
    }

    public static DoctorId of(UUID value) {
        return new DoctorId(value);
    }
}
