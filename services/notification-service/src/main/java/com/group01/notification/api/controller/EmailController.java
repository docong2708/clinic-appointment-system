package com.group01.notification.api.controller;

import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.api.dto.SendEmailResponse;
import com.group01.notification.application.usecase.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for email sending endpoints.
 * Handles internal API calls from business services to send emails.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/emails")
@RequiredArgsConstructor
public class EmailController {

    private final SendEmailUseCase sendEmailUseCase;

    /**
     * Send email via request body.
     * 
     * @param request SendEmailRequest with eventType, recipient, and payload
     * @return SendEmailResponse with notification ID and status
     */
    @PostMapping("/send")
    public ResponseEntity<SendEmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
        try {
            log.info("Received email request: eventType={}, recipientEmail={}", 
                request.getEventType(), request.getRecipientEmail());

            SendEmailResponse response = sendEmailUseCase.execute(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid email request: {}", e.getMessage());
            return ResponseEntity
                .badRequest()
                .body(SendEmailResponse.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            log.error("Failed to send email", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SendEmailResponse.builder()
                    .status("ERROR")
                    .message("Internal server error: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Health check endpoint for email service.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }
}
