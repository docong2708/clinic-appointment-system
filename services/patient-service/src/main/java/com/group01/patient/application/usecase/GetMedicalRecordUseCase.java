package com.group01.patient.application.usecase;

import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.result.MedicalRecordResultMapper;
import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.exception.MedicalRecordNotFoundException;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;

    public GetMedicalRecordUseCase(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional(readOnly = true)
    public MedicalRecordResult execute(UUID id) {
        MedicalRecordAggregate aggregate = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException(id));
        return MedicalRecordResultMapper.from(aggregate);
    }
}
