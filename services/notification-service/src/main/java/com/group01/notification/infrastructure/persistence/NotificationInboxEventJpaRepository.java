package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationInboxEventJpaRepository extends JpaRepository<NotificationInboxEventJpaEntity, UUID> {
    List<NotificationInboxEventJpaEntity> findByStatus(String status);
    List<NotificationInboxEventJpaEntity> findBySourceServiceAndSourceEventId(String sourceService, UUID sourceEventId);
}