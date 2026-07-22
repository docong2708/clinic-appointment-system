package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Query("select d.id from DoctorJpaEntity d where d.userId = :userId")
    java.util.Optional<UUID> findIdByUserId(@Param("userId") UUID userId);
    List<DoctorJpaEntity> findBySpecializationContainingIgnoreCase(String specialization);

    @Modifying
    @Query("UPDATE SlotJpaEntity s SET s.status = com.group01.doctor.domain.model.SlotStatus.AVAILABLE " +
           "WHERE s.status = com.group01.doctor.domain.model.SlotStatus.RESERVED AND s.updatedAt < :cutoffTime")
    int releaseExpiredReservations(@Param("cutoffTime") LocalDateTime cutoffTime);
}
