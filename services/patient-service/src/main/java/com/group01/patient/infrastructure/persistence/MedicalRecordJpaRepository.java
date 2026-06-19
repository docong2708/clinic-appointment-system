package com.group01.patient.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecordJpaEntity, Long> {

    List<MedicalRecordJpaEntity> findByPatientId(Long patientId);
}
