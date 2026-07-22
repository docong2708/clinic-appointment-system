package com.group01.payment.domain.entity;

import com.group01.payment.domain.vo.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentAttempt {

    private final UUID id;
    private final UUID paymentId;
    private final PaymentStatus status;
    private final BigDecimal amount;
    private final String currency;
    private final String requestPayload;
    private final String responsePayload;
    private final String errorMessage;
    private final OffsetDateTime createdAt;

    private PaymentAttempt(
            UUID id,
            UUID paymentId,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String requestPayload,
            String responsePayload,
            String errorMessage,
            OffsetDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Mã lần thử thanh toán không được để trống");
        }
        if (paymentId == null) {
            throw new IllegalArgumentException("Mã thanh toán không được để trống");
        }
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái lần thử thanh toán không được để trống");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Đơn vị tiền tệ không được để trống");
        }

        this.id = id;
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
    }

    public static PaymentAttempt create(
            UUID paymentId,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String requestPayload,
            String responsePayload,
            String errorMessage
    ) {
        return new PaymentAttempt(
                UUID.randomUUID(),
                paymentId,
                status,
                amount,
                currency,
                requestPayload,
                responsePayload,
                errorMessage,
                OffsetDateTime.now()
        );
    }

    public static PaymentAttempt restore(
            UUID id,
            UUID paymentId,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String requestPayload,
            String responsePayload,
            String errorMessage,
            OffsetDateTime createdAt
    ) {
        return new PaymentAttempt(
                id,
                paymentId,
                status,
                amount,
                currency,
                requestPayload,
                responsePayload,
                errorMessage,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
