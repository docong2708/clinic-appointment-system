package com.group01.appointment.application.result;

import java.time.LocalDateTime;

public record RescheduleOptionResult(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long availableCount
) {
}
