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
        processEvent(event.eventId(), "APPOINTMENT_CREATED", event.patientUserId(), event);
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
        processEvent(event.eventId(), "APPOINTMENT_CANCELED", event.patientUserId(), event);
    }

    @RabbitHandler
    @Transactional
    public void handleAppointmentUpdated(AppointmentUpdatedEvent event) {
        log.info("Received AppointmentUpdatedEvent: {}", event.eventId());
        processEvent(event.eventId(), "APPOINTMENT_UPDATED", event.patientUserId(), event);
    }

    private void processEvent(java.util.UUID eventId, String eventType, java.util.UUID recipientId, Object payload) {
        NotificationInboxEvent inboxEvent = null;
        try {
            if (inboxEventRepository.existsBySourceEventId(eventId)) {
                log.info("Event {} already processed, skipping", eventId);
                return;
            }

            inboxEvent = NotificationInboxEvent.create(
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
                    .title(titleFor(eventType))
                    .body(bodyFor(eventType, payload))
                    .priority((short) 1)
                    .channel("EMAIL")
                    .destination(destinationFor(payload))
                    .sourceService("appointment-service")
                    .sourceEventId(eventId)
                    .dedupeKey(eventType)
                    .aggregateType("Appointment")
                    .aggregateId(aggregateId(payload, eventId))
                    .sourceInboxEventId(inboxEvent.getId())
                    .payload(payloadFor(payload))
                    .build();

            createNotificationUseCase.handle(command);

            inboxEvent.markAsProcessed();
            inboxEventRepository.save(inboxEvent);
            log.info("Successfully processed event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to process event: {}", eventId, e);
            if (inboxEvent != null) {
                inboxEvent.markAsFailed(e.getMessage());
                inboxEventRepository.save(inboxEvent);
            }
        }
    }

    private String convertToJson(Object payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String titleFor(String eventType) {
        return switch (eventType) {
            case "APPOINTMENT_CREATED" -> "Appointment request received";
            case "APPOINTMENT_CONFIRMED" -> "Appointment confirmed";
            case "APPOINTMENT_CANCELED" -> "Appointment canceled";
            case "APPOINTMENT_UPDATED" -> "Appointment updated";
            default -> "Appointment notification";
        };
    }

    private String bodyFor(String eventType, Object payload) {
        if (payload instanceof AppointmentCreatedEvent event) {
            return """
                    Your appointment request has been created.

                    Appointment ID: %s
                    Doctor: %s
                    Specialization: %s
                    Time: %s - %s
                    Status: %s
                    Reason: %s
                    """.formatted(
                    event.appointmentId(),
                    hasText(event.doctorName()) ? event.doctorName() : event.doctorId(),
                    hasText(event.doctorSpecialization()) ? event.doctorSpecialization() : "",
                    event.startTime(),
                    event.endTime(),
                    event.status(),
                    event.reason() == null ? "" : event.reason()
            ).trim();
        }

        if (payload instanceof AppointmentConfirmedEvent event) {
            return """
                    Your appointment has been confirmed.

                    Appointment ID: %s
                    Doctor ID: %s
                    Time: %s - %s
                    Status: %s
                    Payment status: %s
                    """.formatted(
                    event.appointmentId(),
                    event.doctorId(),
                    event.startTime(),
                    event.endTime(),
                    event.status(),
                    event.paymentStatus()
            ).trim();
        }

        if (payload instanceof AppointmentCanceledEvent event) {
            return """
                    Your appointment has been canceled.

                    Doctor: %s
                    Specialization: %s
                    Time: %s - %s
                    Status: %s
                    Cancel reason: %s
                    Canceled by: %s
                    """.formatted(
                    hasText(event.doctorName()) ? event.doctorName() : "Clinic doctor",
                    hasText(event.doctorSpecialization()) ? event.doctorSpecialization() : "",
                    event.startTime(),
                    event.endTime(),
                    event.status(),
                    event.cancelReason() == null ? "" : event.cancelReason(),
                    event.cancelledByRole() == null ? "" : event.cancelledByRole()
            ).trim();
        }

        if (payload instanceof AppointmentUpdatedEvent event) {
            return """
                    Your appointment time has been changed.

                    Doctor: %s
                    Specialization: %s
                    Previous time: %s - %s
                    New time: %s - %s
                    Status: %s
                    Reason: %s
                    """.formatted(
                    hasText(event.doctorName()) ? event.doctorName() : "Clinic doctor",
                    hasText(event.doctorSpecialization()) ? event.doctorSpecialization() : "",
                    event.previousStartTime(),
                    event.previousEndTime(),
                    event.startTime(),
                    event.endTime(),
                    event.status(),
                    event.reason() == null ? "" : event.reason()
            ).trim();
        }

        return "Your appointment has been " + eventType.toLowerCase().replace("_", " ") + ".";
    }

    private String destinationFor(Object payload) {
        if (payload instanceof AppointmentCreatedEvent event && hasText(event.patientEmail())) {
            return event.patientEmail();
        }
        if (payload instanceof AppointmentCanceledEvent event && hasText(event.patientEmail())) {
            return event.patientEmail();
        }
        if (payload instanceof AppointmentUpdatedEvent event && hasText(event.patientEmail())) {
            return event.patientEmail();
        }
        return "patient@clinic.com";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private java.util.UUID aggregateId(Object payload, java.util.UUID fallback) {
        if (payload instanceof AppointmentCreatedEvent event) {
            return event.appointmentId();
        }
        if (payload instanceof AppointmentConfirmedEvent event) {
            return event.appointmentId();
        }
        if (payload instanceof AppointmentCanceledEvent event) {
            return event.appointmentId();
        }
        if (payload instanceof AppointmentUpdatedEvent event) {
            return event.appointmentId();
        }
        return fallback;
    }

    private java.util.Map<String, Object> payloadFor(Object payload) {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        if (payload instanceof AppointmentCanceledEvent event) {
            map.put("appointmentId", event.appointmentId() != null ? event.appointmentId().toString() : "");
            map.put("doctorName", hasText(event.doctorName()) ? event.doctorName() : "Bác sĩ phòng khám");
            map.put("patientName", hasText(event.patientEmail()) ? event.patientEmail() : "Bệnh nhân");
            map.put("cancelReason", hasText(event.cancelReason()) ? event.cancelReason() : "Bác sĩ hủy lịch khám");
            map.put("reason", hasText(event.cancelReason()) ? event.cancelReason() : "Bác sĩ hủy lịch khám");
            map.put("appointmentStartTime", event.startTime() != null ? event.startTime().toString() : "");
            map.put("appointmentDateTime", event.startTime() != null ? event.startTime().toString() : "");
        }
        return map;
    }
}
