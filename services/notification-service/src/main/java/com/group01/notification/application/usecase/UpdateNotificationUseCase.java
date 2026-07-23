package com.group01.notification.application.usecase;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.vo.NotificationId;

public interface UpdateNotificationUseCase {
    NotificationAggregate handle(NotificationId notificationId, String title, String body);

    NotificationAggregate markAsRead(NotificationId notificationId);
}