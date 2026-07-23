package com.group01.patient.application.usecase;

import com.group01.patient.application.command.CreatePatientCommand;
import com.group01.patient.application.result.CreatePatientResult;
import com.group01.patient.application.result.PatientResultMapper;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePatientUseCase {

    private final PatientJpaRepository patientJpaRepository;

    public CreatePatientUseCase(PatientJpaRepository patientJpaRepository) {
        this.patientJpaRepository = patientJpaRepository;
    }

    @Transactional
    public CreatePatientResult execute(CreatePatientCommand command) {
        return patientJpaRepository.findByUserId(command.userId())
                .map(patient -> new CreatePatientResult(PatientResultMapper.from(patient), false))
                .orElseGet(() -> createPatient(command));
    }

    private CreatePatientResult createPatient(CreatePatientCommand command) {
        PatientJpaEntity patient = PatientJpaEntity.builder()
                .userId(command.userId())
                .firstName(command.firstName())
                .lastName(command.lastName())
                .dateOfBirth(command.dateOfBirth())
                .gender(command.gender())
                .contactInformation(command.contactInformation())
                .build();

        PatientJpaEntity saved = patientJpaRepository.save(patient);
        return new CreatePatientResult(PatientResultMapper.from(saved), true);
    }
}
