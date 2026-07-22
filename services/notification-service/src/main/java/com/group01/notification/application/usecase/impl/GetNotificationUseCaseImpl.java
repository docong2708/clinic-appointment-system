package com.group01.notification.application.usecase.impl;

import com.group01.notification.application.usecase.GetNotificationUseCase;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.NotificationId;
import org.springframework.stereotype.Service;

@Service
public class GetNotificationUseCaseImpl implements GetNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public GetNotificationUseCaseImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationAggregate handle(NotificationId notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId.value()));
    }
}