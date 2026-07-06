package com.group01.notification.application.usecase;

import com.group01.notification.api.dto.NotificationEventPayload;

public interface ProcessInboxEventUseCase {
    void handle(NotificationEventPayload payload);
}