package com.group01.appointment.domain.vo;

public enum AppointmentLogAction {
    CREATE,
    CANCEL,
    RESCHEDULE,
    PAYMENT_AWAITING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_EXPIRED,
    PAYMENT_DEFERRED,
    COMPLETE
}
