package com.group01.payment.infrastructure.persistence;

import com.group01.payment.domain.entity.PaymentAttempt;
import com.group01.payment.domain.vo.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentAttemptMapper {

    public PaymentAttemptJpaEntity toJpaEntity(PaymentAttempt paymentAttempt) {
        return PaymentAttemptJpaEntity.builder()
                .id(paymentAttempt.getId())
                .paymentId(paymentAttempt.getPaymentId())
                .status(paymentAttempt.getStatus().name())
                .amount(paymentAttempt.getAmount())
                .currency(paymentAttempt.getCurrency())
                .requestPayload(paymentAttempt.getRequestPayload())
                .responsePayload(paymentAttempt.getResponsePayload())
                .errorMessage(paymentAttempt.getErrorMessage())
                .createdAt(paymentAttempt.getCreatedAt())
                .build();
    }

    public PaymentAttempt toDomain(PaymentAttemptJpaEntity entity) {
        return PaymentAttempt.restore(
                entity.getId(),
                entity.getPaymentId(),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getRequestPayload(),
                entity.getResponsePayload(),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }
}
