package com.group01.patient.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateMedicalRecordCommand(
        UUID patientId,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<PrescriptionCommand> prescriptions
) {
    public record PrescriptionCommand(
            String medicationName,
            String dosage,
            String frequency,
            String duration
    ) {}
}

