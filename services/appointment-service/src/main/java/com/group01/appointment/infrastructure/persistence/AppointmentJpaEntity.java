package com.group01.appointment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appointments")
public class AppointmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "slot_id")
    private UUID slotId;

    @Column(name = "rescheduled_from_appointment_id")
    private UUID rescheduledFromAppointmentId;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "cancelled_by_role", length = 50)
    private String cancelledByRole;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "booking_source", length = 50)
    private String bookingSource;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
