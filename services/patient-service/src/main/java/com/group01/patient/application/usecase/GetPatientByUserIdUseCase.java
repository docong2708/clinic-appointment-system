package com.group01.patient.application.usecase;

import com.group01.patient.application.result.PatientResult;
import com.group01.patient.application.result.PatientResultMapper;
import com.group01.patient.domain.exception.PatientNotFoundException;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPatientByUserIdUseCase {

    private final PatientJpaRepository patientJpaRepository;

    public GetPatientByUserIdUseCase(PatientJpaRepository patientJpaRepository) {
        this.patientJpaRepository = patientJpaRepository;
    }

    @Transactional(readOnly = true)
    public PatientResult execute(UUID userId) {
        PatientJpaEntity patient = patientJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("userId", userId));
        return PatientResultMapper.from(patient);
    }
}
