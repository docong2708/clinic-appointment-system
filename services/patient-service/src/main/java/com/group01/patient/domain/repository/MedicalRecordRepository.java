package com.group01.patient.domain.repository;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository {

    MedicalRecordAggregate save(MedicalRecordAggregate aggregate);

    Optional<MedicalRecordAggregate> findById(Long id);

    List<MedicalRecordAggregate> findByPatientId(Long patientId);

    void deleteById(Long id);
}

