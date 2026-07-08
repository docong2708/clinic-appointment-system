package com.group01.notification.infrastructure.sender;

import com.group01.notification.application.port.NotificationSenderPort;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class MockNotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationSenderAdapter.class);

    @Override
    public String send(NotificationAggregate aggregate, NotificationDelivery delivery) throws Exception {
        log.info("[MOCK] Sending notification: [Channel: {}] [To: {}] [Title: {}]", 
                 delivery.getChannel(), 
                 delivery.getDestination(), 
                 aggregate.getTitle());

        NotificationChannel channel = delivery.getChannel();

        switch (channel) {
            case EMAIL:
                log.info("[MOCK] Email sent to: {} - Subject: {}", 
                         delivery.getDestination(), 
                         aggregate.getTitle().value());
                return "mock_email_" + System.currentTimeMillis();
            
            case IN_APP:
                log.info("[MOCK] In-app notification sent. Destination: {}", 
                         delivery.getDestination());
                return "mock_in_app_" + System.currentTimeMillis();
            
            default:
                log.warn("[MOCK] Unsupported channel: {}", channel);
                return "mock_unknown_" + System.currentTimeMillis();
        }
    }
}