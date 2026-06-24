package com.group01.notification.application.usecase.impl;

import com.group01.notification.application.usecase.UpdateNotificationUseCase;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.NotificationId;
import com.group01.notification.domain.vo.NotificationTitle;
import org.springframework.stereotype.Service;

@Service
public class UpdateNotificationUseCaseImpl implements UpdateNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public UpdateNotificationUseCaseImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public NotificationAggregate handle(NotificationId notificationId, String title, String body) {
        NotificationAggregate notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId.value()));
        
        notification.updateContent(NotificationTitle.of(title), body);
        
        return notificationRepository.save(notification);
    }
}