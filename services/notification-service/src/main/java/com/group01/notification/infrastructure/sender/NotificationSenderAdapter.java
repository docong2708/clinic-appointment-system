package com.group01.notification.infrastructure.sender;

import com.group01.notification.application.port.NotificationSenderPort;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationSenderAdapter.class);

    private final EmailSenderService emailSenderService;

    public NotificationSenderAdapter(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @Override
    @Async
    public String send(NotificationAggregate aggregate, NotificationDelivery delivery) throws Exception {
        log.info("Sending notification: [Channel: {}] [To: {}] [Title: {}]", 
                 delivery.getChannel(), 
                 delivery.getDestination(), 
                 aggregate.getTitle());

        NotificationChannel channel = delivery.getChannel();

        switch (channel) {
            case EMAIL:
                return emailSenderService.sendEmail(
                        delivery.getDestination(),
                        aggregate.getTitle().value(),
                        aggregate.getBody()
                );
            
            case IN_APP:
                log.info("In-app notification stored. Destination: {}", delivery.getDestination());
                return "in_app_" + System.currentTimeMillis();
            
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }
}
