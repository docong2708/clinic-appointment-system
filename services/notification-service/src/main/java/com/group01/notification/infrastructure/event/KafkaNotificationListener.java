package com.group01.notification.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group01.notification.api.dto.NotificationEventPayload;
import com.group01.notification.application.usecase.ProcessInboxEventUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationListener.class);

    private final ProcessInboxEventUseCase processInboxEventUseCase;
    private final ObjectMapper objectMapper;

    public KafkaNotificationListener(
            ProcessInboxEventUseCase processInboxEventUseCase,
            ObjectMapper objectMapper
    ) {
        this.processInboxEventUseCase = processInboxEventUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "notification-events", groupId = "notification-service")
    public void listen(String message) {
        log.info("Received Kafka message: {}", message);
        try {
            NotificationEventPayload payload = objectMapper.readValue(message, NotificationEventPayload.class);
            processInboxEventUseCase.handle(payload);
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}