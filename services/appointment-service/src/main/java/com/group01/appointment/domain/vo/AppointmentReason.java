package com.group01.appointment.domain.vo;

public record AppointmentReason(String value) {

    private static final int MAX_LENGTH = 500;

    public AppointmentReason {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Lý do khám không được vượt quá 500 ký tự");
        }
    }

    public static AppointmentReason of(String value) {
        return new AppointmentReason(value);
    }
}
