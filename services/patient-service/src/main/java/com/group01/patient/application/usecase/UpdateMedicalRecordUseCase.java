package com.group01.patient.application.usecase;

import com.group01.patient.application.command.UpdateMedicalRecordCommand;
import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.result.MedicalRecordResultMapper;
import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.entity.Prescription;
import com.group01.patient.domain.exception.MedicalRecordNotFoundException;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UpdateMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;

    public UpdateMedicalRecordUseCase(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional
    public MedicalRecordResult execute(UpdateMedicalRecordCommand command) {
        MedicalRecordAggregate aggregate = medicalRecordRepository.findById(command.id())
                .orElseThrow(() -> new MedicalRecordNotFoundException(command.id()));

        List<Prescription> prescriptions = command.prescriptions() == null
                ? Collections.emptyList()
                : command.prescriptions().stream()
                        .map(p -> Prescription.create(p.medicationName(), p.dosage(), p.frequency(), p.duration()))
                        .toList();

        aggregate.update(command.recordDate(), command.diagnosis(), command.treatment(), command.notes(), prescriptions);

        MedicalRecordAggregate saved = medicalRecordRepository.save(aggregate);

        return MedicalRecordResultMapper.from(saved);
    }
}
