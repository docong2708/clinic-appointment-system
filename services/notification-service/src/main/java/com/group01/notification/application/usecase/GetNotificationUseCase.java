package com.group01.notification.application.usecase;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.vo.NotificationId;

public interface GetNotificationUseCase {
    NotificationAggregate handle(NotificationId notificationId);
}