package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationDeliveryJpaRepository extends JpaRepository<NotificationDeliveryJpaEntity, UUID> {
    List<NotificationDeliveryJpaEntity> findByNotificationId(UUID notificationId);
    List<NotificationDeliveryJpaEntity> findByStatus(String status);
    
    @Query("SELECT d FROM NotificationDeliveryJpaEntity d WHERE d.status IN ('FAILED', 'PENDING') AND d.retryCount < 3 AND (d.nextRetryAt <= :now OR d.nextRetryAt IS NULL)")
    List<NotificationDeliveryJpaEntity> findDeliveriesDueForRetry(@Param("now") OffsetDateTime now);
}