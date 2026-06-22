package com.group01.patient.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MedicalRecord {
    private Long id;
    private Long patientId;
    private LocalDate recordDate;
    private String diagnosis;
    private String treatment;
    private String notes;

    public static MedicalRecord create(
            Long patientId,
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes
    ) {
        return MedicalRecord.builder()
                .patientId(requirePositive(patientId, "Patient id must be positive"))
                .recordDate(requireRecordDate(recordDate))
                .diagnosis(requireText(diagnosis, "Diagnosis is required"))
                .treatment(trimNullable(treatment))
                .notes(trimNullable(notes))
                .build();
    }

    public void update(LocalDate recordDate, String diagnosis, String treatment, String notes) {
        this.recordDate = requireRecordDate(recordDate);
        this.diagnosis = requireText(diagnosis, "Diagnosis is required");
        this.treatment = trimNullable(treatment);
        this.notes = trimNullable(notes);
    }

    private static Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static LocalDate requireRecordDate(LocalDate recordDate) {
        if (recordDate == null) {
            throw new IllegalArgumentException("Record date is required");
        }
        return recordDate;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
