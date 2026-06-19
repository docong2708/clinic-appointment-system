package com.group01.appointment.infrastructure.messaging;

import com.group01.appointment.application.exception.NotificationServiceUnavailableException;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.domain.event.AppointmentCanceledEvent;
import com.group01.appointment.domain.event.AppointmentConfirmedEvent;
import com.group01.appointment.domain.event.AppointmentCreatedEvent;
import com.group01.appointment.domain.event.AppointmentUpdatedEvent;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisherAdapter implements NotificationPort {

    private final NotificationServiceClient notificationServiceClient;

    public AppointmentEventPublisherAdapter(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    @Override
    public void publishAppointmentCreated(AppointmentCreatedEvent event) {
        publish(event);
    }

    @Override
    public void publishAppointmentConfirmed(AppointmentConfirmedEvent event) {
        publish(event);
    }

    @Override
    public void publishAppointmentCanceled(AppointmentCanceledEvent event) {
        publish(event);
    }

    @Override
    public void publishAppointmentUpdated(AppointmentUpdatedEvent event) {
        publish(event);
    }

    private void publish(Object event) {
        try {
            notificationServiceClient.publishAppointmentEvent(event);
        } catch (FeignException exception) {
            throw new NotificationServiceUnavailableException(exception);
        }
    }
}
