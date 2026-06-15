package com.group01.appointment.api.dto;

import com.group01.appointment.application.result.AppointmentResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String cancelReason,
        String status,
        String paymentStatus,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        String bookingSource,
        UUID createdBy,
        UUID updatedBy,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppointmentResponse from(AppointmentResult result) {
        return new AppointmentResponse(
                result.id(),
                result.patientId(),
                result.doctorId(),
                result.slotId(),
                result.rescheduledFromAppointmentId(),
                result.startTime(),
                result.endTime(),
                result.reason(),
                result.cancelReason(),
                result.status(),
                result.paymentStatus(),
                result.cancelledBy(),
                result.cancelledByRole(),
                result.cancelledAt(),
                result.bookingSource(),
                result.createdBy(),
                result.updatedBy(),
                result.confirmedAt(),
                result.completedAt(),
                result.version(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
