package com.group01.appointment.infrastructure.scheduler;

import com.group01.appointment.application.usecase.ExpireAwaitingPaymentAppointmentsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AwaitingPaymentExpirationScheduler {

    private final ExpireAwaitingPaymentAppointmentsUseCase expireAwaitingPaymentAppointmentsUseCase;

    public AwaitingPaymentExpirationScheduler(
            ExpireAwaitingPaymentAppointmentsUseCase expireAwaitingPaymentAppointmentsUseCase
    ) {
        this.expireAwaitingPaymentAppointmentsUseCase = expireAwaitingPaymentAppointmentsUseCase;
    }

    @Scheduled(
            fixedDelayString = "${appointments.payment-expiration.fixed-delay-ms:60000}",
            initialDelayString = "${appointments.payment-expiration.initial-delay-ms:60000}"
    )
    public void expireAwaitingPayments() {
        expireAwaitingPaymentAppointmentsUseCase.execute();
    }
}
