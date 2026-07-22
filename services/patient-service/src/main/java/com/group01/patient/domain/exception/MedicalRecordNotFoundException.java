package com.group01.patient.domain.exception;

import java.util.UUID;

public class MedicalRecordNotFoundException extends RuntimeException {

    public MedicalRecordNotFoundException(UUID id) {
        super("Không tìm thấy hồ sơ khám với mã: " + id);
    }
}
