package com.group01.payment.infrastructure.persistence;

import com.group01.payment.domain.entity.Payment;
import com.group01.payment.domain.vo.PaymentMethod;
import com.group01.payment.domain.vo.PaymentProvider;
import com.group01.payment.domain.vo.PaymentStatus;
import com.group01.payment.domain.vo.PaymentTiming;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentJpaEntity toJpaEntity(Payment payment) {
        return PaymentJpaEntity.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .patientUserId(payment.getPatientUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentTiming(payment.getPaymentTiming().name())
                .status(payment.getStatus().name())
                .method(payment.getMethod().name())
                .provider(payment.getProvider().name())
                .description(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .failedReason(payment.getFailedReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.restore(
                entity.getId(),
                entity.getAppointmentId(),
                entity.getPatientUserId(),
                entity.getAmount(),
                entity.getCurrency(),
                PaymentTiming.valueOf(entity.getPaymentTiming()),
                PaymentStatus.valueOf(entity.getStatus()),
                PaymentMethod.valueOf(entity.getMethod()),
                PaymentProvider.valueOf(entity.getProvider()),
                entity.getDescription(),
                entity.getPaidAt(),
                entity.getFailedReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
