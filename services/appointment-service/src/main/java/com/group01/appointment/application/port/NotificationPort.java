package com.group01.appointment.application.port;

import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;

public interface NotificationPort {

    void publishAppointmentCreated(AppointmentCreatedEvent event);

    void publishAppointmentConfirmed(AppointmentConfirmedEvent event);

    void publishAppointmentCanceled(AppointmentCanceledEvent event);

    void publishAppointmentUpdated(AppointmentUpdatedEvent event);
}