package com.group01.appointment.infrastructure.messaging;

import com.group01.appointment.application.port.NotificationPort;
import com.group01.appointment.infrastructure.client.NotificationServiceClient;
import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;
import com.group01.commonevents.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisherAdapter implements NotificationPort {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    public void publishAppointmentCreated(AppointmentCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_CREATED_ROUTING_KEY,
                event
        );
    }

    @Override
    public void publishAppointmentConfirmed(AppointmentConfirmedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_CONFIRMED_ROUTING_KEY,
                event
        );
    }

    @Override
    public void publishAppointmentCanceled(AppointmentCanceledEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_CANCELED_ROUTING_KEY,
                event
        );
    }

    @Override
    public void publishAppointmentUpdated(AppointmentUpdatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                RabbitMQConstants.APPOINTMENT_UPDATED_ROUTING_KEY,
                event
        );
    }

    @Override
    public void sendDoctorCancellationEmail(DoctorCancellationNotification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("appointmentId", notification.appointmentId().toString());
        payload.put("appointmentDateTime", notification.appointmentStartTime().toString());
        payload.put("appointmentStartTime", notification.appointmentStartTime().toString());
        payload.put("appointmentEndTime", notification.appointmentEndTime().toString());
        payload.put("reason", notification.cancelReason());
        payload.put("patientName", notification.patientName());

        notificationServiceClient.sendAsyncEmail(new NotificationServiceClient.SendEmailRequest(
                UUID.randomUUID().toString(),
                "APPOINTMENT_CANCELED",
                notification.patientEmail(),
                notification.patientName(),
                notification.patientId().toString(),
                payload
        ));
    }
}
