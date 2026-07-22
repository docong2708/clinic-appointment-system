package com.group01.appointment.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select a
            from AppointmentJpaEntity a
            where a.doctorId = :doctorId
              and a.slotId = :slotId
            order by a.createdAt desc
            """)
    List<AppointmentJpaEntity> findByDoctorIdAndSlotId(
            @Param("doctorId") UUID doctorId,
            @Param("slotId") UUID slotId
    );

    @Query("""
            select a
            from AppointmentJpaEntity a
            where a.doctorId = :doctorId
              and a.startTime >= :fromTime
              and a.startTime < :toTime
            order by a.startTime asc
            """)
    List<AppointmentJpaEntity> findDoctorAppointmentsBetween(
            @Param("doctorId") UUID doctorId,
            @Param("fromTime") OffsetDateTime fromTime,
            @Param("toTime") OffsetDateTime toTime
    );

    @Query("""
            select a
            from AppointmentJpaEntity a
            where a.status = 'CONFIRMED'
              and a.startTime < :cutoffTime
            """)
    List<AppointmentJpaEntity> findConfirmedAppointmentsStartedBefore(
            @Param("cutoffTime") OffsetDateTime cutoffTime
    );
}