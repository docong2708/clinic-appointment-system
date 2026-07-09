package com.group01.notification.application.usecase.impl;

import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.api.dto.SendEmailResponse;
import com.group01.notification.application.usecase.SendEmailUseCase;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.repository.NotificationTemplateRepository;
import com.group01.notification.domain.vo.EmailType;
import com.group01.notification.infrastructure.sender.EmailSenderService;
import com.group01.notification.infrastructure.sender.EmailTemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of SendEmailUseCase.
 * Coordinates template loading, rendering, and email delivery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailUseCaseImpl implements SendEmailUseCase {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationRepository notificationRepository;
    private final EmailTemplateRenderer templateRenderer;
    private final EmailSenderService emailSenderService;

    @Override
    public SendEmailResponse execute(SendEmailRequest request) {
        log.info("Processing email request: eventType={}, recipientEmail={}", 
            request.getEventType(), request.getRecipientEmail());

        // Validate email type
        if (!EmailType.isValidEmailType(request.getEventType())) {
            log.warn("Invalid email type: {}", request.getEventType());
            throw new IllegalArgumentException("Invalid email type: " + request.getEventType());
        }

        // Get template key
        String templateKey = new EmailType(request.getEventType()).getTemplateKey();

        // Load template using findByKeyAndActiveTrue
        NotificationTemplate template = templateRepository.findByKeyAndActiveTrue(templateKey)
            .orElseThrow(() -> new RuntimeException("Template not found: " + templateKey));

        // Render subject and body
        String renderedSubject = templateRenderer.render(template.getSubject(), request.getPayload());
        String renderedBody = templateRenderer.render(template.getBody(), request.getPayload());

        // Send email
        String notificationId = UUID.randomUUID().toString();
        String status;
        String message;
        
        try {
            emailSenderService.sendEmail(
                request.getRecipientEmail(),
                renderedSubject,
                renderedBody
            );
            status = "SENT";
            message = "Email queued for delivery";
            log.info("Email {} for type {}: {}", notificationId, request.getEventType(), status);
        } catch (Exception e) {
            status = "FAILED";
            message = "Email delivery failed: " + e.getMessage();
            log.error("Email {} for type {}: {}", notificationId, request.getEventType(), status, e);
        }

        return SendEmailResponse.builder()
            .notificationId(notificationId)
            .status(status)
            .message(message)
            .build();
    }
}
