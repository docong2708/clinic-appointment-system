package com.group01.appointment.domain.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentUpdatedEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        UUID slotId,
        UUID rescheduledFromAppointmentId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        String bookingSource,
        UUID updatedBy,
        LocalDateTime updatedAt
) {

    public static AppointmentUpdatedEvent from(AppointmentAggregate appointment) {
        return new AppointmentUpdatedEvent(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getSlotId(),
                appointment.getRescheduledFromAppointmentId(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null
                        ? null
                        : appointment.getAppointmentReason().value(),
                appointment.getStatus().name(),
                appointment.getBookingSource(),
                appointment.getUpdatedBy(),
                appointment.getUpdatedAt()
        );
    }
}
