package com.group01.notification.application.usecase;

import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.api.dto.SendEmailResponse;

/**
 * Use case for sending emails.
 * Orchestrates template rendering, validation, and email delivery.
 */
public interface SendEmailUseCase {
    SendEmailResponse execute(SendEmailRequest request);
}
