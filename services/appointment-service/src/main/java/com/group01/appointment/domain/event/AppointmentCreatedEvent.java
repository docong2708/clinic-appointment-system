package com.group01.appointment.domain.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCreatedEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        String status,
        String paymentStatus,
        LocalDateTime createdAt
) {

    public static AppointmentCreatedEvent from(AppointmentAggregate appointment) {
        return new AppointmentCreatedEvent(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null
                        ? null
                        : appointment.getAppointmentReason().value(),
                appointment.getStatus().name(),
                appointment.getPaymentStatus() == null
                        ? null
                        : appointment.getPaymentStatus().name(),
                appointment.getCreatedAt()
        );
    }
}
