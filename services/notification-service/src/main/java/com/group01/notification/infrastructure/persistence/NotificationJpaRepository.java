package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    List<NotificationJpaEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    List<NotificationJpaEntity> findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID recipientUserId);
}