package com.group01.payment.application.usecase;

import com.group01.payment.application.command.CreatePaymentCommand;
import com.group01.payment.application.port.AppointmentClientPort;
import com.group01.payment.application.result.PaymentResult;
import com.group01.payment.application.result.PaymentResultMapper;
import com.group01.payment.domain.entity.Payment;
import com.group01.payment.domain.entity.PaymentAttempt;
import com.group01.payment.domain.exception.DuplicatePaymentException;
import com.group01.payment.domain.repository.PaymentAttemptRepository;
import com.group01.payment.domain.repository.PaymentRepository;
import com.group01.payment.domain.vo.PaymentStatus;
import com.group01.payment.domain.vo.PaymentTiming;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreatePaymentUseCase {

    private static final BigDecimal CONSULTATION_FEE = new BigDecimal("100000.00");
    private static final String CURRENCY = "VND";

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final AppointmentClientPort appointmentClientPort;

    public CreatePaymentUseCase(
            PaymentRepository paymentRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            AppointmentClientPort appointmentClientPort
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.appointmentClientPort = appointmentClientPort;
    }

    @Transactional
    public PaymentResult execute(CreatePaymentCommand command) {
        if (command.appointmentId() == null) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống");
        }
        if (command.patientUserId() == null) {
            throw new IllegalArgumentException("Mã người dùng bệnh nhân không được để trống");
        }

        PaymentTiming timing = command.paymentTiming() == null
                ? PaymentTiming.PAY_NOW
                : command.paymentTiming();
        AppointmentClientPort.AppointmentInfo appointment = appointmentClientPort.getAppointment(command.appointmentId());
        validateAppointmentForPayment(appointment, command.patientUserId());

        if (paymentRepository.existsByAppointmentId(command.appointmentId())) {
            throw new DuplicatePaymentException(command.appointmentId());
        }

        Payment payment = Payment.create(
                command.appointmentId(),
                command.patientUserId(),
                CONSULTATION_FEE,
                CURRENCY,
                timing,
                description(timing)
        );
        Payment savedPayment = paymentRepository.save(payment);

        if (timing == PaymentTiming.PAY_LATER) {
            paymentAttemptRepository.save(PaymentAttempt.create(
                    savedPayment.getId(),
                    PaymentStatus.PENDING,
                    savedPayment.getAmount(),
                    savedPayment.getCurrency(),
                    "{\"paymentTiming\":\"PAY_LATER\"}",
                    "{\"message\":\"Đã chọn thanh toán sau tại phòng khám\"}",
                    null
            ));
            appointmentClientPort.markPaymentDeferred(command.appointmentId());
        } else {
            appointmentClientPort.markPaymentAwaiting(command.appointmentId());
        }

        return PaymentResultMapper.from(savedPayment);
    }

    private void validateAppointmentForPayment(
            AppointmentClientPort.AppointmentInfo appointment,
            UUID patientUserId
    ) {
        if ("CANCELLED".equals(appointment.status())) {
            throw new IllegalStateException("Không thể tạo thanh toán cho lịch hẹn đã hủy");
        }
        if ("COMPLETED".equals(appointment.status())) {
            throw new IllegalStateException("Không thể tạo thanh toán mới cho lịch hẹn đã hoàn thành");
        }
        if ("PAYMENT_EXPIRED".equals(appointment.status())) {
            throw new IllegalStateException("Khong the tao thanh toan cho lich hen da qua han thanh toan");
        }
        if ("PAID".equals(appointment.paymentStatus())) {
            throw new IllegalStateException("Lịch hẹn đã được thanh toán");
        }
        if (appointment.createdBy() == null) {
            throw new IllegalStateException("Không xác định được người tạo lịch hẹn");
        }
        if (!appointment.createdBy().equals(patientUserId)) {
            throw new IllegalStateException("Bệnh nhân chỉ được thanh toán lịch hẹn của chính mình");
        }
    }

    private String description(PaymentTiming timing) {
        return timing == PaymentTiming.PAY_LATER
                ? "Thanh toán sau tại phòng khám"
                : "Thanh toán trước bằng mock payment";
    }
}
