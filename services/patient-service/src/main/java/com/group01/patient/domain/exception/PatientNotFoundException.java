package com.group01.patient.domain.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("Không tìm thấy hồ sơ bệnh nhân với mã: " + patientId);
    }

    public PatientNotFoundException(String fieldName, UUID fieldValue) {
        super("Không tìm thấy hồ sơ bệnh nhân theo " + fieldName + ": " + fieldValue);
    }
}
