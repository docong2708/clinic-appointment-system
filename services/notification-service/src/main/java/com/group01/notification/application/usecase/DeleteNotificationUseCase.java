package com.group01.notification.application.usecase;

import com.group01.notification.domain.vo.NotificationId;

public interface DeleteNotificationUseCase {
    void handle(NotificationId notificationId);
}