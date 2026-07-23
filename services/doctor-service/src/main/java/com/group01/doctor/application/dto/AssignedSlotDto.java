package com.group01.doctor.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssignedSlotDto(
        UUID id,
        UUID doctorId,
        UUID doctorUserId,
        String doctorName,
        String specialization,
        String doctorPhoneNumber,
        String doctorEmail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean booked,
        String status
) {
}
