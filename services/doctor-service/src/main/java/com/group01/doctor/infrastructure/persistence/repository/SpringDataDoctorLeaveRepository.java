package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.infrastructure.persistence.entity.DoctorLeaveJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface            SpringDataDoctorLeaveRepository extends JpaRepository<DoctorLeaveJpaEntity, UUID> {
    @Query("""
            select l
            from DoctorLeaveJpaEntity l
            where l.doctor.id = :doctorId
            order by l.startDate desc, l.createdAt desc
            """)
    List<DoctorLeaveJpaEntity> findByDoctorId(@Param("doctorId") UUID doctorId);
}
