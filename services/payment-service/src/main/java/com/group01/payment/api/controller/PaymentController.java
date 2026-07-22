package com.group01.payment.api.controller;

import com.group01.commonsecurity.currentuser.CurrentUser;
import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.payment.api.dto.CreatePaymentRequest;
import com.group01.payment.api.dto.PaymentResponse;
import com.group01.payment.application.command.CreatePaymentCommand;
import com.group01.payment.application.command.PayPaymentCommand;
import com.group01.payment.application.result.PaymentResult;
import com.group01.payment.application.usecase.CreatePaymentUseCase;
import com.group01.payment.application.usecase.GetPaymentByAppointmentUseCase;
import com.group01.payment.application.usecase.GetPaymentUseCase;
import com.group01.payment.application.usecase.PayPaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final GetPaymentByAppointmentUseCase getPaymentByAppointmentUseCase;
    private final PayPaymentUseCase payPaymentUseCase;

    public PaymentController(
            CreatePaymentUseCase createPaymentUseCase,
            GetPaymentUseCase getPaymentUseCase,
            GetPaymentByAppointmentUseCase getPaymentByAppointmentUseCase,
            PayPaymentUseCase payPaymentUseCase
    ) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.getPaymentByAppointmentUseCase = getPaymentByAppointmentUseCase;
        this.payPaymentUseCase = payPaymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CurrentUser currentUser = currentUser();
        PaymentResult result = createPaymentUseCase.execute(new CreatePaymentCommand(
                request.appointmentId(),
                request.paymentTiming(),
                currentUser.userId()
        ));

        return ResponseEntity
                .created(URI.create("/api/payments/" + result.id()))
                .body(PaymentResponse.from(result));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable("paymentId") UUID paymentId) {
        CurrentUser currentUser = currentUser();
        return PaymentResponse.from(getPaymentUseCase.execute(
                paymentId,
                currentUser.userId(),
                actorRole(currentUser)
        ));
    }

    @GetMapping("/appointment/{appointmentId}")
    public PaymentResponse getPaymentByAppointment(@PathVariable("appointmentId") UUID appointmentId) {
        CurrentUser currentUser = currentUser();
        return PaymentResponse.from(getPaymentByAppointmentUseCase.execute(
                appointmentId,
                currentUser.userId(),
                actorRole(currentUser)
        ));
    }

    @PostMapping("/{paymentId}/confirm-paid")
    public ResponseEntity<PaymentResponse> confirmPaid(@PathVariable("paymentId") UUID paymentId) {
        CurrentUser currentUser = currentUser();
        PaymentResult result = payPaymentUseCase.execute(new PayPaymentCommand(
                paymentId,
                currentUser.userId(),
                actorRole(currentUser)
        ));

        return ResponseEntity.ok(PaymentResponse.from(result));
    }

    private CurrentUser currentUser() {
        return CurrentUserHolder.require();
    }

    private String actorRole(CurrentUser currentUser) {
        if (currentUser.hasRole("ADMIN")) {
            return "ADMIN";
        }

        if (currentUser.hasRole("DOCTOR")) {
            return "DOCTOR";
        }

        if (currentUser.hasRole("PATIENT")) {
            return "PATIENT";
        }

        return "USER";
    }
}
