package com.group01.patient.application.command;

import java.time.LocalDate;
import java.util.List;

public record UpdateMedicalRecordCommand(
        Long id,
        LocalDate recordDate,
        String diagnosis,
        String treatment,
        String notes,
        List<CreateMedicalRecordCommand.PrescriptionCommand> prescriptions
) {}
