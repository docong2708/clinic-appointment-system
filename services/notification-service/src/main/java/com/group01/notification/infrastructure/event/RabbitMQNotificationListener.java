package com.group01.notification.infrastructure.event;

import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentConfirmedEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;
import com.group01.commonevents.messaging.RabbitMQConstants;
import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.application.usecase.CreateNotificationUseCase;
import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import com.group01.notification.domain.repository.NotificationInboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RabbitListener(queues = RabbitMQConstants.NOTIFICATION_APPOINTMENT_QUEUE)
public class RabbitMQNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQNotificationListener.class);

    private final NotificationInboxEventRepository inboxEventRepository;
    private final CreateNotificationUseCase createNotificationUseCase;

    public RabbitMQNotificationListener(
            NotificationInboxEventRepository inboxEventRepository,
            CreateNotificationUseCase createNotificationUseCase
    ) {
        this.inboxEventRepository = inboxEventRepository;
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @RabbitHandler
    @Transactional
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Received AppointmentCreatedEvent: {}", event.eventId());
        processEvent(event.eventId(), "APPOINTMENT_CREATED", event.patientId(), event);
    }

    @RabbitHandler
    @Transactional
    public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
        log.info("Received AppointmentConfirmedEvent: {}", event.eventId());
        processEvent(event.eventId(), "APPOINTMENT_CONFIRMED", event.patientId(), event);
    }

    @RabbitHandler
    @Transactional
    public void handleAppointmentCanceled(AppointmentCanceledEvent event) {
        log.info("Received AppointmentCanceledEvent: {}", event.eventId());
        processEvent(event.eventId(), "APPOINTMENT_CANCELED", event.patientId(), event);
    }

    @RabbitHandler
    @Transactional
    public void handleAppointmentUpdated(AppointmentUpdatedEvent event) {
        log.info("Received AppointmentUpdatedEvent: {}", event.eventId());
        processEvent(event.eventId(), "APPOINTMENT_UPDATED", event.patientId(), event);
    }

    private void processEvent(java.util.UUID eventId, String eventType, java.util.UUID recipientId, Object payload) {
        try {
            if (inboxEventRepository.existsBySourceEventId(eventId)) {
                log.info("Event {} already processed, skipping", eventId);
                return;
            }

            NotificationInboxEvent inboxEvent = NotificationInboxEvent.create(
                    "appointment-service",
                    eventId,
                    eventType,
                    "Appointment",
                    eventId,
                    convertToJson(payload),
                    java.util.UUID.randomUUID().toString()
            );
            inboxEvent = inboxEventRepository.save(inboxEvent);

            CreateNotificationCommand command = CreateNotificationCommand.builder()
                    .recipientUserId(recipientId)
                    .type(eventType)
                    .title("Appointment Notification: " + eventType)
                    .body("Your appointment has been " + eventType.toLowerCase().replace("_", " "))
                    .priority((short) 1)
                    .channel("EMAIL")
                    .destination("phudinh193@gmail.com")
                    .build();

            createNotificationUseCase.handle(command);

            inboxEvent.markAsProcessed();
            inboxEventRepository.save(inboxEvent);
            log.info("Successfully processed event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to process event: {}", eventId, e);
            NotificationInboxEvent failedEvent = NotificationInboxEvent.create(
                    "appointment-service",
                    eventId,
                    eventType,
                    "Appointment",
                    eventId,
                    convertToJson(payload),
                    java.util.UUID.randomUUID().toString()
            );
            failedEvent.markAsFailed(e.getMessage());
            inboxEventRepository.save(failedEvent);
        }
    }

    private String convertToJson(Object payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }
}