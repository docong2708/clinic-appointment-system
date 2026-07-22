package com.group01.patient.application.result;

import com.group01.patient.infrastructure.persistence.PatientJpaEntity;

public class PatientResultMapper {

    private PatientResultMapper() {
    }

    public static PatientResult from(PatientJpaEntity entity) {
        return new PatientResult(
                entity.getId(),
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getGender(),
                entity.getContactInformation()
        );
    }
}
