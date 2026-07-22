package com.group01.appointment.domain.vo;

import java.time.LocalDateTime;

public record AppointmentTime(
        LocalDateTime startTime,
        LocalDateTime endTime
) {

    public AppointmentTime {
        if (startTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu không được để trống");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("Thời gian kết thúc không được để trống");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }

    public static AppointmentTime of(LocalDateTime startTime, LocalDateTime endTime) {
        return new AppointmentTime(startTime, endTime);
    }
}
