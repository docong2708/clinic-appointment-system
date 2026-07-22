package com.group01.patient.application.usecase;

import com.group01.patient.domain.exception.MedicalRecordNotFoundException;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;

    public DeleteMedicalRecordUseCase(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (medicalRecordRepository.findById(id).isEmpty()) {
            throw new MedicalRecordNotFoundException(id);
        }
        medicalRecordRepository.deleteById(id);
    }
}
