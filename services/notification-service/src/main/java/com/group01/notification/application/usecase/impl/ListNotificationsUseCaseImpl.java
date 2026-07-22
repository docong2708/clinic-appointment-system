package com.group01.notification.application.usecase.impl;

import com.group01.notification.application.usecase.ListNotificationsUseCase;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ListNotificationsUseCaseImpl implements ListNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public ListNotificationsUseCaseImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationAggregate> handleByRecipientId(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    @Override
    public List<NotificationAggregate> handleUnreadByRecipientId(UUID recipientId) {
        return notificationRepository.findUnreadByRecipientId(recipientId);
    }
}