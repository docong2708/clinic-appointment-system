package com.group01.notification.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for email events.
 */
@Configuration
public class EmailRabbitMQConfig {

    public static final String EMAIL_QUEUE = "email.send.queue";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }
}
