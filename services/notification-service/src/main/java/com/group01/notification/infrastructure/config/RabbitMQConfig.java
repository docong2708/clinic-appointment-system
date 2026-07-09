package com.group01.notification.infrastructure.config;

import com.group01.commonevents.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(
                RabbitMQConstants.APPOINTMENT_EXCHANGE,
                true,
                false
        );
    }

    @Bean(name = "notificationQueue")
    public Queue notificationQueue() {
        return new Queue(
                RabbitMQConstants.NOTIFICATION_APPOINTMENT_QUEUE,
                true
        );
    }

    @Bean
    public Binding notificationBinding(
            @Qualifier("notificationQueue") Queue notificationQueue,
            TopicExchange appointmentExchange
    ) {
        return BindingBuilder.bind(notificationQueue)
                .to(appointmentExchange)
                .with(RabbitMQConstants.APPOINTMENT_ROUTING_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
