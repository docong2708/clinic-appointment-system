package com.group01.payment.application.port;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentClientPort {

    AppointmentInfo getAppointment(UUID appointmentId);

    void markPaymentAwaiting(UUID appointmentId);

    void markPaymentPaid(UUID appointmentId);

    void markPaymentFailed(UUID appointmentId);

    void markPaymentDeferred(UUID appointmentId);

    record AppointmentInfo(
            UUID id,
            UUID patientId,
            UUID doctorId,
            UUID slotId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            String paymentStatus,
            UUID createdBy
    ) {
    }
}
