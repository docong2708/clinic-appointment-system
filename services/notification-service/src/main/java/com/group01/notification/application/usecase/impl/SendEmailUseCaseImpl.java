package com.group01.notification.application.usecase.impl;

import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.api.dto.SendEmailResponse;
import com.group01.notification.application.usecase.SendEmailUseCase;
import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.repository.NotificationTemplateRepository;
import com.group01.notification.domain.vo.EmailType;
import com.group01.notification.infrastructure.sender.EmailSenderService;
import com.group01.notification.infrastructure.sender.EmailTemplateRenderer;
import com.group01.notification.infrastructure.sender.FileEmailTemplateService;
import com.group01.notification.infrastructure.sender.FileEmailTemplateService.EmailTemplateContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
    private final FileEmailTemplateService fileEmailTemplateService;
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

        EmailTemplateContent template = resolveTemplate(templateKey);

        // Render subject and body
        String renderedSubject = templateRenderer.render(template.subject(), request.getPayload());
        String renderedBody = templateRenderer.render(template.body(), request.getPayload());

        // Send email
        String notificationId = UUID.randomUUID().toString();
        String status;
        String message;
        
        try {
            if (template.html()) {
                emailSenderService.sendHtmlEmail(
                    request.getRecipientEmail(),
                    renderedSubject,
                    renderedBody
                );
            } else {
                emailSenderService.sendEmail(
                    request.getRecipientEmail(),
                    renderedSubject,
                    renderedBody
                );
            }
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

    private EmailTemplateContent resolveTemplate(String templateKey) {
        Optional<EmailTemplateContent> fileTemplate = fileEmailTemplateService.findByKey(templateKey);
        if (fileTemplate.isPresent()) {
            return fileTemplate.get();
        }

        NotificationTemplate databaseTemplate = templateRepository.findByKeyAndActiveTrue(templateKey)
            .orElseThrow(() -> new RuntimeException("Template not found: " + templateKey));
        return new EmailTemplateContent(databaseTemplate.getSubject(), databaseTemplate.getBody(), false);
    }
}
