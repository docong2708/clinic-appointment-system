package com.group01.patient.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecordJpaEntity, UUID> {

    List<MedicalRecordJpaEntity> findByPatientId(UUID patientId);
}
