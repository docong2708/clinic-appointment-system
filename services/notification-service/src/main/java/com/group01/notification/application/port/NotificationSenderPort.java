package com.group01.notification.application.port;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;

public interface NotificationSenderPort {
    String send(NotificationAggregate aggregate, NotificationDelivery delivery) throws Exception;
}
