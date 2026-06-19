package com.group01.patient.application.result;

import com.group01.patient.domain.aggregate.MedicalRecordAggregate;
import com.group01.patient.domain.entity.Prescription;

import java.util.Collections;
import java.util.List;

public class MedicalRecordResultMapper {

    private MedicalRecordResultMapper() {}

    public static MedicalRecordResult from(MedicalRecordAggregate aggregate) {
        List<MedicalRecordResult.PrescriptionResult> prescriptions = aggregate.getPrescriptions() == null
                ? Collections.emptyList()
                : aggregate.getPrescriptions().stream()
                        .map(MedicalRecordResultMapper::toPrescriptionResult)
                        .toList();

        return new MedicalRecordResult(
                aggregate.getMedicalRecord().getId(),
                aggregate.getMedicalRecord().getPatientId(),
                aggregate.getMedicalRecord().getRecordDate(),
                aggregate.getMedicalRecord().getDiagnosis(),
                aggregate.getMedicalRecord().getTreatment(),
                aggregate.getMedicalRecord().getNotes(),
                prescriptions
        );
    }

    private static MedicalRecordResult.PrescriptionResult toPrescriptionResult(Prescription prescription) {
        return new MedicalRecordResult.PrescriptionResult(
                prescription.getId(),
                prescription.getMedicalRecordId(),
                prescription.getMedicationName(),
                prescription.getDosage(),
                prescription.getFrequency(),
                prescription.getDuration()
        );
    }
}
