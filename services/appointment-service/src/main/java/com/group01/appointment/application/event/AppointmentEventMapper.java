package com.group01.appointment.application.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AppointmentEventMapper {

    private AppointmentEventMapper() {
    }

    public static AppointmentCreatedEvent created(AppointmentAggregate appointment, String patientEmail) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentCreatedEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                patientEmail,
                appointment.getDoctorId().value(),
                appointment.getSlotId(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null
                        ? null
                        : appointment.getAppointmentReason().value(),
                appointment.getStatus().name(),
                occurredAt
        );
    }

    public static AppointmentCanceledEvent canceled(AppointmentAggregate appointment) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentCanceledEvent(
                UUID.randomUUID(),
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
                appointment.getStatus().name(),
                occurredAt
        );
    }

    public static AppointmentConfirmedEvent confirmed(AppointmentAggregate appointment) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentConfirmedEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getStatus().name(),
                appointment.getPaymentStatus() == null
                        ? null
                        : appointment.getPaymentStatus().name(),
                appointment.getConfirmedAt(),
                occurredAt
        );
    }

    public static AppointmentUpdatedEvent updated(AppointmentAggregate appointment) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentUpdatedEvent(
                UUID.randomUUID(),
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
                appointment.getUpdatedAt(),
                occurredAt
        );
    }
}
