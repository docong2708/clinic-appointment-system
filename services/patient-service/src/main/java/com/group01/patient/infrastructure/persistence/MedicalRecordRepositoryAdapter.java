package com.group01.patient.infrastructure.persistence;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MedicalRecordRepositoryAdapter implements MedicalRecordRepository {

    private final MedicalRecordJpaRepository medicalRecordJpaRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    public MedicalRecordRepositoryAdapter(
            MedicalRecordJpaRepository medicalRecordJpaRepository,
            MedicalRecordMapper medicalRecordMapper
    ) {
        this.medicalRecordJpaRepository = medicalRecordJpaRepository;
        this.medicalRecordMapper = medicalRecordMapper;
    }

    @Override
    public MedicalRecordAggregate save(MedicalRecordAggregate aggregate) {
        MedicalRecordJpaEntity entity = medicalRecordMapper.toJpaEntity(aggregate);
        MedicalRecordJpaEntity savedEntity = medicalRecordJpaRepository.save(entity);
        return medicalRecordMapper.toAggregate(savedEntity);
    }

    @Override
    public Optional<MedicalRecordAggregate> findById(Long id) {
        return medicalRecordJpaRepository.findById(id)
                .map(medicalRecordMapper::toAggregate);
    }

    @Override
    public List<MedicalRecordAggregate> findByPatientId(Long patientId) {
        return medicalRecordJpaRepository.findByPatientId(patientId)
                .stream()
                .map(medicalRecordMapper::toAggregate)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        medicalRecordJpaRepository.deleteById(id);
    }
}
