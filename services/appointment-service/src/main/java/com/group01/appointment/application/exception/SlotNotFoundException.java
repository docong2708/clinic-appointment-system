package com.group01.appointment.application.exception;

import java.util.UUID;

public class SlotNotFoundException extends ResourceNotFoundException {

    public SlotNotFoundException(UUID doctorId, UUID slotId) {
        super("Không tìm thấy khung giờ " + slotId + " của bác sĩ " + doctorId);
    }
}
