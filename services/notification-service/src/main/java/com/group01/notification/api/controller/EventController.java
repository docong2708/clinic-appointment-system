package com.group01.notification.api.controller;

import com.group01.notification.api.dto.NotificationEventPayload;
import com.group01.notification.application.usecase.ProcessInboxEventUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private final ProcessInboxEventUseCase processInboxEventUseCase;

    public EventController(ProcessInboxEventUseCase processInboxEventUseCase) {
        this.processInboxEventUseCase = processInboxEventUseCase;
    }

    @PostMapping("/appointment")
    public ResponseEntity<Void> receiveAppointmentEvent(@RequestBody NotificationEventPayload payload) {
        log.info("Received appointment event: {}", payload.getEventId());
        try {
            processInboxEventUseCase.handle(payload);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test-send")
    public ResponseEntity<String> testSendToEmail() {
        log.info("Test send to ddinhphu2004@gmail.com");
        try {
            NotificationEventPayload payload = NotificationEventPayload.builder()
                    .sourceService("test-service")
                    .eventId(UUID.randomUUID())
                    .eventType("TEST_EVENT")
                    .recipientId(UUID.randomUUID())
                    .aggregateId(UUID.randomUUID())
                    .aggregateType("Test")
                    .eventTime(LocalDateTime.now())
                    .payload("Test notification to ddinhphu2004@gmail.com")
                    .build();
            processInboxEventUseCase.handle(payload);
            return ResponseEntity.ok("Test email sent!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}