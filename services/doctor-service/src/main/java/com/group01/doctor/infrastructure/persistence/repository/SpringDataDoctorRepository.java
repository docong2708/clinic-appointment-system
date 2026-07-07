package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
