package com.group01.patient.infrastructure.persistence;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.entity.MedicalRecord;
import com.group01.patient.domain.entity.Prescription;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MedicalRecordMapper {

    public MedicalRecordJpaEntity toJpaEntity(MedicalRecordAggregate aggregate) {
        List<PrescriptionJpaEntity> prescriptionEntities = aggregate.getPrescriptions() == null
                ? Collections.emptyList()
                : aggregate.getPrescriptions().stream()
                        .map(this::toPrescriptionJpaEntity)
                        .toList();

        return MedicalRecordJpaEntity.builder()
                .id(aggregate.getMedicalRecord().getId())
                .patientId(aggregate.getMedicalRecord().getPatientId())
                .recordDate(aggregate.getMedicalRecord().getRecordDate())
                .diagnosis(aggregate.getMedicalRecord().getDiagnosis())
                .treatment(aggregate.getMedicalRecord().getTreatment())
                .notes(aggregate.getMedicalRecord().getNotes())
                .prescriptions(prescriptionEntities)
                .build();
    }

    public MedicalRecordAggregate toAggregate(MedicalRecordJpaEntity entity) {
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .recordDate(entity.getRecordDate())
                .diagnosis(entity.getDiagnosis())
                .treatment(entity.getTreatment())
                .notes(entity.getNotes())
                .build();

        List<Prescription> prescriptions = entity.getPrescriptions() == null
                ? Collections.emptyList()
                : entity.getPrescriptions().stream()
                        .map(this::toPrescription)
                        .toList();

        return new MedicalRecordAggregate(medicalRecord, prescriptions);
    }

    private PrescriptionJpaEntity toPrescriptionJpaEntity(Prescription prescription) {
        return PrescriptionJpaEntity.builder()
                .id(prescription.getId())
                .medicalRecordId(prescription.getMedicalRecordId())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .duration(prescription.getDuration())
                .build();
    }

    private Prescription toPrescription(PrescriptionJpaEntity entity) {
        return Prescription.builder()
                .id(entity.getId())
                .medicalRecordId(entity.getMedicalRecordId())
                .medicationName(entity.getMedicationName())
                .dosage(entity.getDosage())
                .frequency(entity.getFrequency())
                .duration(entity.getDuration())
                .build();
    }
}
