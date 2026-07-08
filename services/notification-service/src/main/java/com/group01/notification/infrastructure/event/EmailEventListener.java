package com.group01.notification.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group01.notification.api.dto.SendEmailRequest;
import com.group01.notification.application.usecase.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ listener for email events.
 * Processes async email sending requests from message queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventListener {

    private final SendEmailUseCase sendEmailUseCase;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "email.send.queue")
    public void handleEmailEvent(String message) {
        try {
            log.info("Received email event: {}", message);
            
            SendEmailRequest request = objectMapper.readValue(message, SendEmailRequest.class);
            
            sendEmailUseCase.execute(request);
            
            log.info("Email event processed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process email event: {}", message, e);
        }
    }
}
