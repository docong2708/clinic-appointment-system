package com.group01.notification.domain.repository;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.vo.NotificationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    NotificationAggregate save(NotificationAggregate aggregate);
    Optional<NotificationAggregate> findById(NotificationId id);
    List<NotificationAggregate> findByRecipientId(UUID recipientId);
    List<NotificationAggregate> findUnreadByRecipientId(UUID recipientId);
    void deleteById(NotificationId id);
}
