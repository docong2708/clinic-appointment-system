package com.group01.notification.domain.repository;

import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import java.util.Optional;
import java.util.UUID;

public interface NotificationInboxEventRepository {
    NotificationInboxEvent save(NotificationInboxEvent event);
    Optional<NotificationInboxEvent> findById(UUID id);
    boolean existsBySourceEventId(UUID sourceEventId);
}
