package com.group01.patient.api.dto;

import com.group01.patient.application.result.PatientResult;
import com.group01.patient.infrastructure.persistence.PatientJpaEntity;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String contactInformation
) {
    public static PatientResponse from(PatientJpaEntity entity) {
        return new PatientResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getContactInformation()
        );
    }

    public static PatientResponse from(PatientResult result) {
        return new PatientResponse(
                result.id(),
                result.userId(),
                result.firstName(),
                result.lastName(),
                result.dateOfBirth(),
                result.gender(),
                result.contactInformation()
        );
    }
}
