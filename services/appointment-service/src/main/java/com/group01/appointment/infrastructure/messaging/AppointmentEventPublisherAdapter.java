package com.group01.appointment.infrastructure.messaging;

import com.group01.appointment.application.port.NotificationPort;
import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;
import com.group01.commonevents.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisherAdapter implements NotificationPort {

    private final RabbitTemplate rabbitTemplate;

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
}
