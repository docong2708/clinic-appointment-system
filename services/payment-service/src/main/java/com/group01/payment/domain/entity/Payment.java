package com.group01.payment.domain.entity;

import com.group01.payment.domain.vo.PaymentMethod;
import com.group01.payment.domain.vo.PaymentProvider;
import com.group01.payment.domain.vo.PaymentStatus;
import com.group01.payment.domain.vo.PaymentTiming;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final UUID appointmentId;
    private final UUID patientUserId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentTiming paymentTiming;
    private PaymentStatus status;
    private PaymentMethod method;
    private final PaymentProvider provider;
    private final String description;
    private OffsetDateTime paidAt;
    private String failedReason;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Payment(
            UUID id,
            UUID appointmentId,
            UUID patientUserId,
            BigDecimal amount,
            String currency,
            PaymentTiming paymentTiming,
            PaymentStatus status,
            PaymentMethod method,
            PaymentProvider provider,
            String description,
            OffsetDateTime paidAt,
            String failedReason,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Mã thanh toán không được để trống");
        }
        if (appointmentId == null) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống");
        }
        if (patientUserId == null) {
            throw new IllegalArgumentException("Mã người dùng bệnh nhân không được để trống");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Đơn vị tiền tệ không được để trống");
        }
        if (paymentTiming == null) {
            throw new IllegalArgumentException("Hình thức thanh toán không được để trống");
        }
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái thanh toán không được để trống");
        }
        if (method == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Nhà cung cấp thanh toán không được để trống");
        }

        this.id = id;
        this.appointmentId = appointmentId;
        this.patientUserId = patientUserId;
        this.amount = amount;
        this.currency = currency;
        this.paymentTiming = paymentTiming;
        this.status = status;
        this.method = method;
        this.provider = provider;
        this.description = description;
        this.paidAt = paidAt;
        this.failedReason = failedReason;
        this.createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static Payment create(
            UUID appointmentId,
            UUID patientUserId,
            BigDecimal amount,
            String currency,
            PaymentTiming paymentTiming,
            String description
    ) {
        PaymentMethod method = paymentTiming == PaymentTiming.PAY_LATER
                ? PaymentMethod.CASH
                : PaymentMethod.MOCK;
        return new Payment(
                UUID.randomUUID(),
                appointmentId,
                patientUserId,
                amount,
                currency,
                paymentTiming,
                PaymentStatus.PENDING,
                method,
                PaymentProvider.INTERNAL,
                description,
                null,
                null,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    public static Payment restore(
            UUID id,
            UUID appointmentId,
            UUID patientUserId,
            BigDecimal amount,
            String currency,
            PaymentTiming paymentTiming,
            PaymentStatus status,
            PaymentMethod method,
            PaymentProvider provider,
            String description,
            OffsetDateTime paidAt,
            String failedReason,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        return new Payment(
                id,
                appointmentId,
                patientUserId,
                amount,
                currency,
                paymentTiming,
                status,
                method,
                provider,
                description,
                paidAt,
                failedReason,
                createdAt,
                updatedAt
        );
    }

    public void markPaid() {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Thanh toán đã được xác nhận trước đó");
        }
        if (status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thanh toán giao dịch đã hủy");
        }
        if (status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Không thể thanh toán giao dịch đã hoàn tiền");
        }
        if (status == PaymentStatus.EXPIRED) {
            throw new IllegalStateException("Khong the thanh toan giao dich da qua han");
        }
        this.status = PaymentStatus.PAID;
        this.failedReason = null;
        this.paidAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void markFailed(String reason) {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Không thể đánh dấu lỗi cho thanh toán đã thành công");
        }
        if (status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể đánh dấu lỗi cho thanh toán đã hủy");
        }
        if (status == PaymentStatus.EXPIRED) {
            throw new IllegalStateException("Khong the danh dau loi cho thanh toan da qua han");
        }
        this.status = PaymentStatus.FAILED;
        this.failedReason = reason;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markCancelled(String reason) {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Không thể hủy thanh toán đã thành công");
        }
        this.status = PaymentStatus.CANCELLED;
        this.failedReason = reason;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markExpired(String reason) {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Khong the qua han thanh toan da thanh cong");
        }
        if (status == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Khong the qua han thanh toan da huy");
        }
        if (status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Khong the qua han thanh toan da hoan tien");
        }
        if (status == PaymentStatus.EXPIRED) {
            return;
        }

        this.status = PaymentStatus.EXPIRED;
        this.failedReason = reason;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public UUID getPatientUserId() {
        return patientUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentTiming getPaymentTiming() {
        return paymentTiming;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
