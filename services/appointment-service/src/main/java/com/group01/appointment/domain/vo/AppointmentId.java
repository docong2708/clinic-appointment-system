package com.group01.appointment.domain.vo;

import java.util.UUID;

public record AppointmentId(UUID value) {

    public AppointmentId {
        if (value == null) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống");
        }
    }

    public static AppointmentId of(UUID value) {
        return new AppointmentId(value);
    }

    public static AppointmentId newId() {
        return new AppointmentId(UUID.randomUUID());
    }
}
