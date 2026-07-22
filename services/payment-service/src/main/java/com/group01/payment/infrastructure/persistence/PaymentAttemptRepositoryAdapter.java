package com.group01.payment.infrastructure.persistence;

import com.group01.payment.domain.entity.PaymentAttempt;
import com.group01.payment.domain.repository.PaymentAttemptRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class PaymentAttemptRepositoryAdapter implements PaymentAttemptRepository {

    private final PaymentAttemptJpaRepository paymentAttemptJpaRepository;
    private final PaymentAttemptMapper paymentAttemptMapper;

    public PaymentAttemptRepositoryAdapter(
            PaymentAttemptJpaRepository paymentAttemptJpaRepository,
            PaymentAttemptMapper paymentAttemptMapper
    ) {
        this.paymentAttemptJpaRepository = paymentAttemptJpaRepository;
        this.paymentAttemptMapper = paymentAttemptMapper;
    }

    @Override
    public PaymentAttempt save(PaymentAttempt paymentAttempt) {
        PaymentAttemptJpaEntity entity = paymentAttemptMapper.toJpaEntity(paymentAttempt);
        PaymentAttemptJpaEntity savedEntity = paymentAttemptJpaRepository.save(entity);
        return paymentAttemptMapper.toDomain(savedEntity);
    }

    @Override
    public List<PaymentAttempt> findByPaymentId(UUID paymentId) {
        return paymentAttemptJpaRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
                .stream()
                .map(paymentAttemptMapper::toDomain)
                .toList();
    }
}
