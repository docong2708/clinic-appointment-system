package com.group01.appointment.domain.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentConfirmedEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String paymentStatus,
        LocalDateTime confirmedAt
) {

    public static AppointmentConfirmedEvent from(AppointmentAggregate appointment) {
        return new AppointmentConfirmedEvent(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getStatus().name(),
                appointment.getPaymentStatus() == null
                        ? null
                        : appointment.getPaymentStatus().name(),
                appointment.getUpdatedAt()
        );
    }
}
