package com.group01.payment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "patient_user_id", nullable = false)
    private UUID patientUserId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "payment_timing", nullable = false, length = 30)
    private String paymentTiming;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "failed_reason", length = 255)
    private String failedReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
