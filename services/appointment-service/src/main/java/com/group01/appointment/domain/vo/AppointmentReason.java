package com.group01.appointment.domain.vo;

public record AppointmentReason(String value) {

    private static final int MAX_LENGTH = 500;

    public AppointmentReason {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Appointment reason must not exceed 500 characters");
        }
    }

    public static AppointmentReason of(String value) {
        return new AppointmentReason(value);
    }
}
