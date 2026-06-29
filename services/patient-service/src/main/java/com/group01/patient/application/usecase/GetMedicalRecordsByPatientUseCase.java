package com.group01.patient.application.usecase;

import com.group01.patient.application.result.MedicalRecordResult;
import com.group01.patient.application.result.MedicalRecordResultMapper;
import com.group01.patient.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetMedicalRecordsByPatientUseCase {

    private final MedicalRecordRepository medicalRecordRepository;

    public GetMedicalRecordsByPatientUseCase(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResult> execute(UUID patientId) {
        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(MedicalRecordResultMapper::from)
                .toList();
    }
}
