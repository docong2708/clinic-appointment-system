package com.group01.payment.application.exception;

import java.util.UUID;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("Không tìm thấy lịch hẹn: " + appointmentId);
    }
}
