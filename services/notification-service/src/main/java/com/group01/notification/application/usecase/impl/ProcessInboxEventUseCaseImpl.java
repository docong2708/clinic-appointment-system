package com.group01.notification.application.usecase.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group01.notification.api.dto.NotificationEventPayload;
import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.application.usecase.CreateNotificationUseCase;
import com.group01.notification.application.usecase.ProcessInboxEventUseCase;
import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import com.group01.notification.domain.repository.NotificationInboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessInboxEventUseCaseImpl implements ProcessInboxEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessInboxEventUseCaseImpl.class);

    private final NotificationInboxEventRepository inboxEventRepository;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final ObjectMapper objectMapper;

    public ProcessInboxEventUseCaseImpl(
            NotificationInboxEventRepository inboxEventRepository,
            CreateNotificationUseCase createNotificationUseCase,
            ObjectMapper objectMapper
    ) {
        this.inboxEventRepository = inboxEventRepository;
        this.createNotificationUseCase = createNotificationUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void handle(NotificationEventPayload payload) {
        log.info("Processing inbox event: {}", payload.getEventId());

        if (inboxEventRepository.existsBySourceEventId(payload.getEventId())) {
            log.info("Event {} already processed, skipping", payload.getEventId());
            return;
        }

        String payloadJson = "{}";
        try {
            if (payload.getPayload() != null) {
                payloadJson = objectMapper.writeValueAsString(payload.getPayload());
            }
        } catch (Exception e) {
            log.warn("Failed to serialize payload: {}", e.getMessage());
        }

        NotificationInboxEvent inboxEvent = NotificationInboxEvent.create(
                payload.getSourceService(),
                payload.getEventId(),
                payload.getEventType(),
                payload.getAggregateType(),
                payload.getAggregateId(),
                payloadJson,
                UUID.randomUUID().toString()
        );
        inboxEvent = inboxEventRepository.save(inboxEvent);

        try {
            CreateNotificationCommand command = CreateNotificationCommand.builder()
                    .recipientUserId(payload.getRecipientId())
                    .type(payload.getEventType())
                    .title("System Notification: " + payload.getEventType())
                    .body("Event processed from " + payload.getSourceService() + ". Detail: " + payloadJson)
                    .priority((short) 1)
                    .channel("EMAIL")
                    .destination("phudinh193@gmail.com")
                    .sourceService(payload.getSourceService())
                    .sourceEventId(payload.getEventId())
                    .dedupeKey(payload.getEventType())
                    .aggregateType(payload.getAggregateType())
                    .aggregateId(payload.getAggregateId())
                    .sourceInboxEventId(inboxEvent.getId())
                    .build();

            createNotificationUseCase.handle(command);

            inboxEvent.markAsProcessed();
            inboxEventRepository.save(inboxEvent);
            log.info("Successfully processed inbox event: {}", payload.getEventId());
        } catch (Exception e) {
            log.error("Failed to process inbox event: {}", payload.getEventId(), e);
            inboxEvent.markAsFailed(e.getMessage());
            inboxEventRepository.save(inboxEvent);
        }
    }
}