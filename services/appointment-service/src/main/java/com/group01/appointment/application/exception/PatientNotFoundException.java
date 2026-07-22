package com.group01.appointment.application.exception;

import java.util.UUID;

public class PatientNotFoundException extends ResourceNotFoundException {

    public PatientNotFoundException(UUID patientId) {
        super("Không tìm thấy hồ sơ bệnh nhân: " + patientId);
    }
}
