package com.group01.patient.domain.aggregate;

import com.group01.patient.domain.entity.MedicalRecord;
import com.group01.patient.domain.entity.Prescription;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class MedicalRecordAggregate {
    private final MedicalRecord medicalRecord;
    private final List<Prescription> prescriptions;

    public MedicalRecordAggregate(MedicalRecord medicalRecord, List<Prescription> prescriptions) {
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Medical record is required");
        }
        this.medicalRecord = medicalRecord;
        this.prescriptions = prescriptions == null ? new ArrayList<>() : new ArrayList<>(prescriptions);
    }

    public static MedicalRecordAggregate create(
            UUID patientId,
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes,
            List<Prescription> prescriptions
    ) {
        return new MedicalRecordAggregate(
                MedicalRecord.create(patientId, recordDate, diagnosis, treatment, notes),
                prescriptions
        );
    }

    public void update(LocalDate recordDate, String diagnosis, String treatment, String notes, List<Prescription> prescriptions) {
        medicalRecord.update(recordDate, diagnosis, treatment, notes);
        this.prescriptions.clear();
        if (prescriptions != null) {
            this.prescriptions.addAll(prescriptions);
        }
    }
}
