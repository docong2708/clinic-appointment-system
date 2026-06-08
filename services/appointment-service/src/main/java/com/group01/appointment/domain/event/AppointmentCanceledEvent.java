package com.group01.appointment.domain.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCanceledEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        String cancelReason,
        UUID cancelledBy,
        String cancelledByRole,
        LocalDateTime cancelledAt,
        String status
) {

    public static AppointmentCanceledEvent from(AppointmentAggregate appointment) {
        return new AppointmentCanceledEvent(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getCancelReason() == null
                        ? null
                        : appointment.getCancelReason().value(),
                appointment.getCancelledBy(),
                appointment.getCancelledByRole() == null
                        ? null
                        : appointment.getCancelledByRole().name(),
                appointment.getCancelledAt(),
                appointment.getStatus().name()
        );
    }
}
