package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.domain.model.AvailableSlot;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataDoctorRepository extends JpaRepository<DoctorJpaEntity, UUID> {
    boolean existsByUserId(UUID userId);
    List<DoctorJpaEntity> findBySpecializationIgnoreCaseAndActiveTrue(String specialization);

    @Query("""
            select distinct d.specialization
            from DoctorJpaEntity d
            where d.active = true and d.specialization is not null and trim(d.specialization) <> ''
            order by d.specialization
            """)
    List<String> findDistinctActiveSpecializations();

    java.util.Optional<DoctorJpaEntity> findByUserId(UUID userId);
    List<DoctorJpaEntity> findBySpecializationContainingIgnoreCase(String specialization);

    @Query("""
            select new com.group01.doctor.domain.model.AvailableSlot(s.startTime, s.endTime, count(s.id))
            from SlotJpaEntity s
            join s.doctor d
            where lower(d.specialization) = lower(:specialization)
              and d.active = true
              and s.status = com.group01.doctor.domain.model.SlotStatus.AVAILABLE
              and s.startTime >= :from
              and s.startTime < :to
            group by s.startTime, s.endTime
            order by s.startTime asc, s.endTime asc
            """)
    List<AvailableSlot> findAvailableSlotsBySpecialization(
            @Param("specialization") String specialization,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SlotJpaEntity s
            join fetch s.doctor d
            where lower(d.specialization) = lower(:specialization)
              and d.active = true
              and s.status = com.group01.doctor.domain.model.SlotStatus.AVAILABLE
              and s.startTime = :startTime
              and s.endTime = :endTime
            order by d.name asc, d.id asc
            """)
    List<SlotJpaEntity> findAssignableSlots(
            @Param("specialization") String specialization,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE SlotJpaEntity s SET s.status = com.group01.doctor.domain.model.SlotStatus.AVAILABLE " +
           "WHERE s.status = com.group01.doctor.domain.model.SlotStatus.RESERVED AND s.updatedAt < :cutoffTime")
    int releaseExpiredReservations(@Param("cutoffTime") LocalDateTime cutoffTime);
}
