package com.group01.payment.infrastructure.scheduler;

import com.group01.payment.application.usecase.ExpirePendingPayNowPaymentsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PendingPayNowPaymentExpirationScheduler {

    private final ExpirePendingPayNowPaymentsUseCase expirePendingPayNowPaymentsUseCase;

    public PendingPayNowPaymentExpirationScheduler(
            ExpirePendingPayNowPaymentsUseCase expirePendingPayNowPaymentsUseCase
    ) {
        this.expirePendingPayNowPaymentsUseCase = expirePendingPayNowPaymentsUseCase;
    }

    @Scheduled(
            fixedDelayString = "${payments.expiration.fixed-delay-ms:60000}",
            initialDelayString = "${payments.expiration.initial-delay-ms:60000}"
    )
    public void expirePendingPayNowPayments() {
        expirePendingPayNowPaymentsUseCase.execute();
    }
}
