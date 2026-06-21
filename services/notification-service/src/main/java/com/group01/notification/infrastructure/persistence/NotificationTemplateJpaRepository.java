package com.group01.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface NotificationTemplateJpaRepository extends JpaRepository<NotificationTemplateJpaEntity, UUID> {
    Optional<NotificationTemplateJpaEntity> findByNameAndActiveTrue(String name);
    List<NotificationTemplateJpaEntity> findByChannelAndActiveTrue(String channel);
}