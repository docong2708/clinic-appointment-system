package com.group01.payment.infrastructure.persistence;

import com.group01.payment.domain.entity.Payment;
import com.group01.payment.domain.repository.PaymentRepository;
import com.group01.payment.domain.vo.PaymentStatus;
import com.group01.payment.domain.vo.PaymentTiming;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;

    public PaymentRepositoryAdapter(
            PaymentJpaRepository paymentJpaRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentMapper.toJpaEntity(payment);
        PaymentJpaEntity savedEntity = paymentJpaRepository.save(entity);
        return paymentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByAppointmentId(UUID appointmentId) {
        return paymentJpaRepository.findByAppointmentId(appointmentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findPendingPayNowCreatedBefore(OffsetDateTime createdBefore, int limit) {
        return paymentJpaRepository.findByPaymentTimingAndStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                        PaymentTiming.PAY_NOW.name(),
                        PaymentStatus.PENDING.name(),
                        createdBefore,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByAppointmentId(UUID appointmentId) {
        return paymentJpaRepository.existsByAppointmentId(appointmentId);
    }
}
