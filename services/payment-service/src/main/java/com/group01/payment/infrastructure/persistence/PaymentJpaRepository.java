package com.group01.payment.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByAppointmentId(UUID appointmentId);

    List<PaymentJpaEntity> findByPaymentTimingAndStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            String paymentTiming,
            String status,
            OffsetDateTime createdAt,
            Pageable pageable
    );

    boolean existsByAppointmentId(UUID appointmentId);
}
