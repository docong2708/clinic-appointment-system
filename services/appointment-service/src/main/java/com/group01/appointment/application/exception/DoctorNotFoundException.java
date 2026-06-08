package com.group01.appointment.application.exception;

import java.util.UUID;

public class DoctorNotFoundException extends ResourceNotFoundException {

    public DoctorNotFoundException(UUID doctorId) {
        super("Doctor not found: " + doctorId);
    }
}
