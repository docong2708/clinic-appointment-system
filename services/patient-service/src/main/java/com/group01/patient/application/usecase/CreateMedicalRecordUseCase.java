package com.group01.patient.application.usecase;

import com.group01.patient.application.command.CreateMedicalRecordCommand;
import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.result.MedicalRecordResultMapper;
import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.entity.Prescription;
import com.group01.patient.domain.exception.PatientNotFoundException;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import com.group01.patient.infrastructure.persistence.PatientJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class CreateMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientJpaRepository patientJpaRepository;

    public CreateMedicalRecordUseCase(
            MedicalRecordRepository medicalRecordRepository,
            PatientJpaRepository patientJpaRepository
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientJpaRepository = patientJpaRepository;
    }

    @Transactional
    public MedicalRecordResult execute(CreateMedicalRecordCommand command) {
        if (!patientJpaRepository.existsById(command.patientId())) {
            throw new PatientNotFoundException(command.patientId());
        }

        List<Prescription> prescriptions = command.prescriptions() == null
                ? Collections.emptyList()
                : command.prescriptions().stream()
                        .map(p -> Prescription.create(p.medicationName(), p.dosage(), p.frequency(), p.duration()))
                        .toList();

        MedicalRecordAggregate aggregate = MedicalRecordAggregate.create(
                command.patientId(),
                command.recordDate(),
                command.diagnosis(),
                command.treatment(),
                command.notes(),
                prescriptions
        );

        MedicalRecordAggregate saved = medicalRecordRepository.save(aggregate);

        return MedicalRecordResultMapper.from(saved);
    }
}

