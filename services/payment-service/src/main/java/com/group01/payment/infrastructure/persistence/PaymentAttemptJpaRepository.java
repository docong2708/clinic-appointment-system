package com.group01.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentAttemptJpaRepository extends JpaRepository<PaymentAttemptJpaEntity, UUID> {

    List<PaymentAttemptJpaEntity> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
}
