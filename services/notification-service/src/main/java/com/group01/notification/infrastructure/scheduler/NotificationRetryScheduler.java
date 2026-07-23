package com.group01.notification.infrastructure.scheduler;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.NotificationId;
import com.group01.notification.infrastructure.persistence.NotificationDeliveryJpaEntity;
import com.group01.notification.infrastructure.persistence.NotificationDeliveryJpaRepository;
import com.group01.notification.infrastructure.persistence.NotificationMapper;
import com.group01.notification.application.port.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

    private final NotificationDeliveryJpaRepository deliveryJpaRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSenderPort senderPort;
    private final NotificationMapper mapper;

    public NotificationRetryScheduler(
            NotificationDeliveryJpaRepository deliveryJpaRepository,
            NotificationRepository notificationRepository,
            NotificationSenderPort senderPort,
            NotificationMapper mapper
    ) {
        this.deliveryJpaRepository = deliveryJpaRepository;
        this.notificationRepository = notificationRepository;
        this.senderPort = senderPort;
        this.mapper = mapper;
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void retryFailedDeliveries() {
        log.debug("Starting retry scheduler job");
        
        try {
            List<NotificationDeliveryJpaEntity> deliveries = 
                deliveryJpaRepository.findDeliveriesDueForRetry(OffsetDateTime.now());
            
            if (deliveries.isEmpty()) {
                log.debug("No deliveries due for retry");
                return;
            }

            log.info("Found {} deliveries to retry", deliveries.size());

            for (NotificationDeliveryJpaEntity deliveryEntity : deliveries) {
                retryDelivery(deliveryEntity);
            }
        } catch (Exception e) {
            log.error("Error in retry scheduler", e);
        }
    }

    private void retryDelivery(NotificationDeliveryJpaEntity deliveryEntity) {
        try {
            NotificationDelivery delivery = mapper.deliveryToDomain(deliveryEntity);
            Optional<NotificationAggregate> notif = notificationRepository.findById(
                NotificationId.of(deliveryEntity.getNotificationId())
            );

            if (notif.isEmpty()) {
                log.warn("Không tìm thấy thông báo cho lượt gửi: {}", deliveryEntity.getId());
                return;
            }

            NotificationAggregate notification = notif.get();
            delivery.markAsSending();
            
            String providerMessageId = senderPort.send(notification, delivery);
            delivery.markAsSent(providerMessageId);
            
            deliveryJpaRepository.save(mapper.deliveryToJpa(delivery));
            log.info("Successfully retried delivery: {}", deliveryEntity.getId());

        } catch (Exception e) {
            log.error("Failed to retry delivery {}: {}", deliveryEntity.getId(), e.getMessage());
            NotificationDelivery delivery = mapper.deliveryToDomain(deliveryEntity);
            delivery.markAsFailed(e.getMessage());
            deliveryJpaRepository.save(mapper.deliveryToJpa(delivery));
        }
    }
}
