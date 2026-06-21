package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, UUID> {
    List<NotificationPreferenceJpaEntity> findByUserId(UUID userId);
    List<NotificationPreferenceJpaEntity> findByUserIdAndChannel(UUID userId, String channel);
}