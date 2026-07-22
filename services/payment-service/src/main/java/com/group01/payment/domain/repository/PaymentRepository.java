package com.group01.payment.domain.repository;

import com.group01.payment.domain.entity.Payment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByAppointmentId(UUID appointmentId);

    List<Payment> findPendingPayNowCreatedBefore(OffsetDateTime createdBefore, int limit);

    boolean existsByAppointmentId(UUID appointmentId);
}
