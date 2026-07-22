package com.group01.patient.application.result;

public record CreatePatientResult(
        PatientResult patient,
        boolean created
) {
}
