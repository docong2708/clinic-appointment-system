package com.group01.appointment.application.port;

import java.util.UUID;

public interface PatientClientPort {

    boolean existsById(UUID patientId);
}
