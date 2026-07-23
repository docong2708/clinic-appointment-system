package com.group01.appointment.api.dto;

import com.group01.appointment.application.result.RescheduleOptionResult;

import java.time.LocalDateTime;

public record RescheduleOptionResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long availableCount
) {
    public static RescheduleOptionResponse from(RescheduleOptionResult result) {
        return new RescheduleOptionResponse(
                result.startTime(),
                result.endTime(),
                result.availableCount()
        );
    }
}
