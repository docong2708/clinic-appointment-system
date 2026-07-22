package com.group01.appointment.domain.vo;

public enum AppointmentStatus {
    PENDING_PAYMENT,
    PENDING_DOCTOR_CONFIRMATION,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    CANCELLED_BY_DOCTOR,
    NOT_CHECKIN,
    CHECKIN_SUCCESS,
    CHECKOUT_SUCCESS
}
