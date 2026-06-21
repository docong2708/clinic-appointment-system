package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationDeliveryJpaRepository extends JpaRepository<NotificationDeliveryJpaEntity, UUID> {
    List<NotificationDeliveryJpaEntity> findByNotificationId(UUID notificationId);
    List<NotificationDeliveryJpaEntity> findByStatus(String status);
}