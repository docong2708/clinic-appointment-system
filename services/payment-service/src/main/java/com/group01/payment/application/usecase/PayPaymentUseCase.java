package com.group01.payment.application.usecase;

import com.group01.payment.application.command.PayPaymentCommand;
import com.group01.payment.application.port.AppointmentClientPort;
import com.group01.payment.application.result.PaymentResult;
import com.group01.payment.application.result.PaymentResultMapper;
import com.group01.payment.domain.entity.Payment;
import com.group01.payment.domain.entity.PaymentAttempt;
import com.group01.payment.domain.exception.PaymentNotFoundException;
import com.group01.payment.domain.repository.PaymentAttemptRepository;
import com.group01.payment.domain.repository.PaymentRepository;
import com.group01.payment.domain.vo.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final AppointmentClientPort appointmentClientPort;

    public PayPaymentUseCase(
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            AppointmentClientPort appointmentClientPort
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.appointmentClientPort = appointmentClientPort;
    }

    @Transactional
    public PaymentResult execute(PayPaymentCommand command) {
        if (command.paymentId() == null) {
            throw new IllegalArgumentException("Mã thanh toán không được để trống");
        }
        if (command.performedBy() == null) {
            throw new IllegalArgumentException("Mã người thao tác không được để trống");
        }
        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(command.paymentId()));

        PaymentAuthorization.requireOwnerOrAdmin(payment, command.performedBy(), command.performedByRole());
        payment.markPaid();
        Payment savedPayment = paymentRepository.save(payment);
        paymentAttemptRepository.save(PaymentAttempt.create(
                savedPayment.getId(),
                PaymentStatus.PAID,
                savedPayment.getAmount(),
                savedPayment.getCurrency(),
                "{\"action\":\"CONFIRM_PAID\",\"performedBy\":\"" + command.performedBy() + "\"}",
                "{\"message\":\"Người dùng đã bấm xác nhận đã thanh toán\"}",
                null
        ));
        appointmentClientPort.markPaymentPaid(savedPayment.getAppointmentId());

        return PaymentResultMapper.from(savedPayment);
    }
}
