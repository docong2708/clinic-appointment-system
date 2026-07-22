package com.group01.appointment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service-client", url = "${clients.notification-service.base-url:http://localhost:8083}")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/internal/emails/async/send")
    void sendAsyncEmail(@RequestBody SendEmailRequest request);

    record SendEmailRequest(
            String eventId,
            String eventType,
            String recipientEmail,
            String recipientName,
            String recipientUserId,
            Map<String, Object> payload
    ) {
    }
}
