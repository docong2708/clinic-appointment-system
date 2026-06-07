package com.group01.appointment.domain.vo;

import java.time.LocalDateTime;

public record AppointmentTime(
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    public AppointmentTime {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("End time must not be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    public static AppointmentTime of(LocalDateTime startTime, LocalDateTime endTime) {
        return new AppointmentTime(startTime, endTime);
    }
}