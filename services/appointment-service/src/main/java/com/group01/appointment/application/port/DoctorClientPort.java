package com.group01.appointment.application.port;

import java.util.UUID;

public interface DoctorClientPort {

    boolean existsById(UUID doctorId);
}
