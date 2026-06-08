package com.group01.appointment.application.result;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;

public class AppointmentResultMapper {

    private AppointmentResultMapper() {
    }

    public static AppointmentResult from(AppointmentAggregate appointment) {
        return new AppointmentResult(
                appointment.getAppointmentId().value(),
                appointment.getPatientId().value(),
                appointment.getDoctorId().value(),
                appointment.getAppointmentTime().startTime(),
                appointment.getAppointmentTime().endTime(),
                appointment.getAppointmentReason() == null ? null : appointment.getAppointmentReason().value(),
                appointment.getCancelReason() == null ? null : appointment.getCancelReason().value(),
                appointment.getStatus().name(),
                appointment.getPaymentStatus() == null ? null : appointment.getPaymentStatus().name(),
                appointment.getCancelledBy(),
                appointment.getCancelledByRole() == null ? null : appointment.getCancelledByRole().name(),
                appointment.getCancelledAt(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}