package com.group01.appointment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "appointment_logs")
public class AppointmentLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "performed_by_role", length = 50)
    private String performedByRole;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
