package com.group01.notification.application.usecase;

import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.application.port.NotificationSenderPort;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessNotificationUseCase implements CreateNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessNotificationUseCase.class);

    private final NotificationRepository notificationRepository;
    private final NotificationSenderPort senderPort;

    public ProcessNotificationUseCase(
            NotificationRepository notificationRepository,
            NotificationSenderPort senderPort
    ) {
        this.notificationRepository = notificationRepository;
        this.senderPort = senderPort;
    }

    @Override
    @Transactional
    public NotificationAggregate handle(CreateNotificationCommand command) {
        log.info("Processing notification creation for user: {}", command.getRecipientUserId());

        // 1. Khởi tạo Aggregate
        NotificationAggregate notification = NotificationAggregate.create(
                RecipientId.of(command.getRecipientUserId()),
                command.getType(),
                NotificationTitle.of(command.getTitle()),
                command.getBody(),
                command.getPriority(),
                command.getSourceService(),
                command.getSourceEventId(),
                command.getDedupeKey(),
                command.getAggregateType(),
                command.getAggregateId(),
                command.getSourceInboxEventId()
        );

        if (command.getPayload() != null && !command.getPayload().isEmpty()) {
            notification.setPayload(command.getPayload());
        }

        // 2. Tạo Delivery
        NotificationChannel channel = NotificationChannel.valueOf(command.getChannel());
        NotificationDelivery delivery = NotificationDelivery.create(
                notification.getId(),
                channel,
                command.getDestination()
        );
        
        notification.addDelivery(delivery);
        notification.markAsReady();

        // 3. Lưu Database
        NotificationAggregate saved = notificationRepository.save(notification);

        // 4. Gửi notification qua Outbound Port (Bất đồng bộ hoặc Đồng bộ)
        // Lấy delivery đã lưu để cập nhật trạng thái gửi
        NotificationDelivery savedDelivery = saved.getDeliveries().get(0);
        try {
            savedDelivery.markAsSending();
            String providerMessageId = senderPort.send(saved, savedDelivery);
            savedDelivery.markAsSent(providerMessageId);
            saved.markAsSent();
            log.info("Notification sent successfully. Provider ID: {}", providerMessageId);
        } catch (Exception e) {
            log.error("Failed to send notification via channel: {}", channel, e);
            savedDelivery.markAsFailed(e.getMessage());
            saved.markAsFailed();
        }

        // 5. Lưu lại trạng thái cập nhật (sent / failed)
        return notificationRepository.save(saved);
    }
}