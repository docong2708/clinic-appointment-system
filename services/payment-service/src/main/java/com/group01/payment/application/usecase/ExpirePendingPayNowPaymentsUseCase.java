package com.group01.payment.application.usecase;

import com.group01.payment.domain.entity.Payment;
import com.group01.payment.domain.entity.PaymentAttempt;
import com.group01.payment.domain.repository.PaymentAttemptRepository;
import com.group01.payment.domain.repository.PaymentRepository;
import com.group01.payment.domain.vo.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExpirePendingPayNowPaymentsUseCase {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(15);
    private static final int BATCH_SIZE = 50;

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public ExpirePendingPayNowPaymentsUseCase(
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Transactional
    public int execute() {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(PAYMENT_TIMEOUT);
        List<Payment> payments = paymentRepository.findPendingPayNowCreatedBefore(cutoff, BATCH_SIZE);

        for (Payment payment : payments) {
            payment.markExpired("Payment timeout expired");
            Payment savedPayment = paymentRepository.save(payment);
            paymentAttemptRepository.save(PaymentAttempt.create(
                    savedPayment.getId(),
                    PaymentStatus.EXPIRED,
                    savedPayment.getAmount(),
                    savedPayment.getCurrency(),
                    "{\"action\":\"EXPIRE_PAYMENT\"}",
                    "{\"message\":\"Payment timeout expired\"}",
                    null
            ));
        }

        return payments.size();
    }
}
