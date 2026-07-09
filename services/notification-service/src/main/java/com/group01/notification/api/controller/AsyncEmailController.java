package com.group01.notification.api.controller;

import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.api.dto.SendEmailResponse;
import com.group01.notification.infrastructure.event.EmailEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for async email sending via RabbitMQ.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/emails/async")
@RequiredArgsConstructor
public class AsyncEmailController {

    private final EmailEventPublisher emailEventPublisher;

    /**
     * Send email asynchronously via RabbitMQ queue.
     */
    @PostMapping("/send")
    public ResponseEntity<SendEmailResponse> sendEmailAsync(@RequestBody SendEmailRequest request) {
        try {
            log.info("Publishing async email request: eventType={}, recipient={}", 
                request.getEventType(), request.getRecipientEmail());

            emailEventPublisher.publishEmailEvent(request);

            return ResponseEntity.accepted()
                .body(SendEmailResponse.builder()
                    .notificationId(request.getEventId())
                    .status("QUEUED")
                    .message("Email queued for async processing")
                    .build());

        } catch (Exception e) {
            log.error("Failed to queue email", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SendEmailResponse.builder()
                    .status("ERROR")
                    .message("Failed to queue email: " + e.getMessage())
                    .build());
        }
    }
}
