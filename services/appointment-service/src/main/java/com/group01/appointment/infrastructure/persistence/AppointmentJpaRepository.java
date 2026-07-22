package com.group01.appointment.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, UUID> {
    List<AppointmentJpaEntity> findByPatientIdOrderByStartTimeDesc(UUID patientId);

    List<AppointmentJpaEntity> findByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            String status,
            OffsetDateTime updatedAt,
            Pageable pageable
    );
}
