package com.group01.appointment.domain.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentUpdatedEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        LocalDateTime updatedAt
) {

    public static AppointmentUpdatedEvent from(AppointmentAggregate appointment) {
        return new AppointmentUpdatedEvent(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null
                        ? null
                        : appointment.getAppointmentReason().value(),
                appointment.getStatus().name(),
                appointment.getUpdatedAt()
        );
    }
}
