package com.group01.appointment.domain.valueobject;

import java.util.UUID;

public record AppointmentId(UUID value) {

    public AppointmentId {
        if (value == null) {
            throw new IllegalArgumentException("Appointment id must not be null");
        }
    }

    public static AppointmentId of(UUID value) {
        return new AppointmentId(value);
    }

    public static AppointmentId newId() {
        return new AppointmentId(UUID.randomUUID());
    }
}
