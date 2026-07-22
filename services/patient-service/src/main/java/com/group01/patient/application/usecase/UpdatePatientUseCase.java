package com.group01.patient.application.usecase;

import com.group01.patient.application.command.UpdatePatientCommand;
import com.group01.patient.application.result.PatientResult;
import com.group01.patient.application.result.PatientResultMapper;
import com.group01.patient.domain.exception.PatientNotFoundException;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdatePatientUseCase {

    private final PatientJpaRepository patientJpaRepository;

    public UpdatePatientUseCase(PatientJpaRepository patientJpaRepository) {
        this.patientJpaRepository = patientJpaRepository;
    }

    @Transactional
    public PatientResult updateById(UUID patientId, UpdatePatientCommand command) {
        PatientJpaEntity patient = patientJpaRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
        apply(patient, command);
        return PatientResultMapper.from(patientJpaRepository.save(patient));
    }

    @Transactional
    public PatientResult upsertByUserId(UUID userId, UpdatePatientCommand command) {
        PatientJpaEntity patient = patientJpaRepository.findByUserId(userId)
                .orElseGet(() -> PatientJpaEntity.builder()
                        .userId(userId)
                        .build());
        apply(patient, command);
        return PatientResultMapper.from(patientJpaRepository.save(patient));
    }

    private void apply(PatientJpaEntity patient, UpdatePatientCommand command) {
        patient.setFirstName(command.firstName());
        patient.setLastName(command.lastName());
        patient.setDateOfBirth(command.dateOfBirth());
        patient.setGender(command.gender());
        patient.setContactInformation(command.contactInformation());
    }
}
