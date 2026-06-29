package com.group01.appointment.application.port;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DoctorClientPort {

    boolean existsById(UUID doctorId);

    DoctorSlot getSlot(UUID doctorId, UUID slotId);

    DoctorSlot bookSlot(UUID doctorId, UUID slotId);

    void cancelSlotBooking(UUID doctorId, UUID slotId);

    record DoctorSlot(
            UUID id,
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked
    ) {
    }
}
