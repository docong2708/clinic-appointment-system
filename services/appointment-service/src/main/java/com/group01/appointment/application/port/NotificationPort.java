package com.group01.appointment.application.port;

import com.group01.appointment.domain.event.AppointmentCanceledEvent;
import com.group01.appointment.domain.event.AppointmentConfirmedEvent;
import com.group01.appointment.domain.event.AppointmentCreatedEvent;
import com.group01.appointment.domain.event.AppointmentUpdatedEvent;

public interface NotificationPort {

    void publishAppointmentCreated(AppointmentCreatedEvent event);

    void publishAppointmentConfirmed(AppointmentConfirmedEvent event);

    void publishAppointmentCanceled(AppointmentCanceledEvent event);

    void publishAppointmentUpdated(AppointmentUpdatedEvent event);
}
