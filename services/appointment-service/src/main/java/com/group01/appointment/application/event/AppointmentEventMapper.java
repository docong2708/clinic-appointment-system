package com.group01.appointment.application.event;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.application.port.DoctorClientPort.DoctorProfile;
import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AppointmentEventMapper {

    private AppointmentEventMapper() {
    }

    public static AppointmentCreatedEvent created(
            AppointmentAggregate appointment,
            String patientEmail,
            UUID patientUserId,
            DoctorProfile doctor
    ) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentCreatedEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                patientUserId,
                appointment.getPatientId().value(),
                patientEmail,
                appointment.getDoctorId().value(),
                doctor.name(),
                doctor.specialization(),
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

    public static AppointmentCanceledEvent canceled(
            AppointmentAggregate appointment,
            AppointmentNotificationDetails details
    ) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentCanceledEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                details.patientUserId(),
                appointment.getPatientId().value(),
                details.patientEmail(),
                appointment.getDoctorId().value(),
                details.doctorName(),
                details.doctorSpecialization(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
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

    public static AppointmentConfirmedEvent confirmed(
            AppointmentAggregate appointment,
            AppointmentNotificationDetails details
    ) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentConfirmedEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                details.patientUserId(),
                appointment.getPatientId().value(),
                details.patientEmail(),
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

    public static AppointmentUpdatedEvent updated(
            AppointmentAggregate appointment,
            AppointmentNotificationDetails details,
            UUID previousSlotId,
            LocalDateTime previousStartTime,
            LocalDateTime previousEndTime
    ) {
        LocalDateTime occurredAt = LocalDateTime.now();

        return new AppointmentUpdatedEvent(
                UUID.randomUUID(),
                appointment.getAppointmentId().value(),
                details.patientUserId(),
                appointment.getPatientId().value(),
                details.patientEmail(),
                appointment.getDoctorId().value(),
                details.doctorName(),
                details.doctorSpecialization(),
                previousSlotId,
                previousStartTime,
                previousEndTime,
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
