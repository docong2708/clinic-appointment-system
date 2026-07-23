package com.group01.doctor.domain.model;

import java.time.LocalDateTime;

public record AvailableSlot(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long availableCount
) {
}
