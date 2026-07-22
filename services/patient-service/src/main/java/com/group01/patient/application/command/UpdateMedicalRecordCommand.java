package com.group01.patient.application.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateMedicalRecordCommand(
        UUID id,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<CreateMedicalRecordCommand.PrescriptionCommand> prescriptions
) {}
