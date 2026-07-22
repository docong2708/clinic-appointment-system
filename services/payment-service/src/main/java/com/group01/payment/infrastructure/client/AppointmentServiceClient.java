package com.group01.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@FeignClient(name = "payment-appointment-service-client", url = "${clients.appointment-service.base-url:http://localhost:8081}")
public interface AppointmentServiceClient {

    @GetMapping("/api/appointments/{appointmentId}")
    AppointmentResponse getAppointment(@PathVariable("appointmentId") UUID appointmentId);

    @PostMapping("/api/appointments/{appointmentId}/payment-awaiting")
    AppointmentResponse markPaymentAwaiting(@PathVariable("appointmentId") UUID appointmentId);

    @PostMapping("/api/appointments/{appointmentId}/payment-paid")
    AppointmentResponse markPaymentPaid(@PathVariable("appointmentId") UUID appointmentId);

    @PostMapping("/api/appointments/{appointmentId}/payment-failed")
    AppointmentResponse markPaymentFailed(@PathVariable("appointmentId") UUID appointmentId);

    @PostMapping("/api/appointments/{appointmentId}/payment-deferred")
    AppointmentResponse markPaymentDeferred(@PathVariable("appointmentId") UUID appointmentId);

    record AppointmentResponse(
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
