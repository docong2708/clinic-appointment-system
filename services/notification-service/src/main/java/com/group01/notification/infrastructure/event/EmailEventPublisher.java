package com.group01.notification.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group01.notification.api.dto.SendEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publisher for email events to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishEmailEvent(SendEmailRequest request) {
        try {
            String message = objectMapper.writeValueAsString(request);
            rabbitTemplate.convertAndSend("email.send.queue", message);
            log.info("Email event published: eventType={}, recipient={}", 
                request.getEventType(), request.getRecipientEmail());
        } catch (Exception e) {
            log.error("Failed to publish email event", e);
            throw new RuntimeException("Failed to publish email event", e);
        }
    }
}
