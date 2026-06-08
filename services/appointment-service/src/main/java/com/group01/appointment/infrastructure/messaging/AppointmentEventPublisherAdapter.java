package com.group01.appointment.infrastructure.messaging;

import com.group01.appointment.application.exception.NotificationServiceUnavailableException;
import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.domain.event.AppointmentCanceledEvent;
import com.group01.appointment.domain.event.AppointmentConfirmedEvent;
import com.group01.appointment.domain.event.AppointmentCreatedEvent;
import com.group01.appointment.domain.event.AppointmentUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AppointmentEventPublisherAdapter implements NotificationPort {

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;
    private final String appointmentEventsPath;

    public AppointmentEventPublisherAdapter(
            RestTemplate restTemplate,
            @Value("${clients.notification-service.base-url}") String notificationServiceBaseUrl,
            @Value("${clients.notification-service.appointment-events-path}") String appointmentEventsPath
    ) {
        this.restTemplate = restTemplate;
        this.notificationServiceBaseUrl = trimTrailingSlash(notificationServiceBaseUrl);
        this.appointmentEventsPath = normalizePath(appointmentEventsPath);
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
            restTemplate.postForEntity(
                    notificationServiceBaseUrl + appointmentEventsPath,
                    event,
                    Void.class
            );
        } catch (RestClientException exception) {
            throw new NotificationServiceUnavailableException(exception);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    private String normalizePath(String value) {
        if (value.startsWith("/")) {
            return value;
        }

        return "/" + value;
    }
}
