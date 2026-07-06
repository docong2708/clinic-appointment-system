package com.group01.notification.application.usecase;

import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.domain.aggregate.NotificationAggregate;

public interface CreateNotificationUseCase {
    NotificationAggregate handle(CreateNotificationCommand command);
}
