package com.group01.patient.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MedicalRecord {
    private UUID id;
    private UUID patientId;
    private LocalDate recordDate;
    private String diagnosis;
    private String treatment;
    private String notes;

    public static MedicalRecord create(
            UUID patientId,
            LocalDate recordDate,
            String diagnosis,
            String treatment,
            String notes
    ) {
        return MedicalRecord.builder()
                .patientId(requirePatientId(patientId))
                .recordDate(requireRecordDate(recordDate))
                .diagnosis(requireText(diagnosis, "Chẩn đoán không được để trống"))
                .treatment(trimNullable(treatment))
                .notes(trimNullable(notes))
                .build();
    }

    public void update(LocalDate recordDate, String diagnosis, String treatment, String notes) {
        this.recordDate = requireRecordDate(recordDate);
        this.diagnosis = requireText(diagnosis, "Chẩn đoán không được để trống");
        this.treatment = trimNullable(treatment);
        this.notes = trimNullable(notes);
    }

    private static UUID requirePatientId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Mã bệnh nhân không được để trống");
        }
        return value;
    }

    private static LocalDate requireRecordDate(LocalDate recordDate) {
        if (recordDate == null) {
            throw new IllegalArgumentException("Ngày ghi nhận hồ sơ không được để trống");
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
