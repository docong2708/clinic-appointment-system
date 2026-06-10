package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataDoctorRepository extends JpaRepository<DoctorJpaEntity, UUID> {
}
