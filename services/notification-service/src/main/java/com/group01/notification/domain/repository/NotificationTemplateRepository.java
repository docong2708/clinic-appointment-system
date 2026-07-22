package com.group01.notification.domain.repository;

import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.vo.NotificationChannel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository {
    void save(NotificationTemplate template);
    Optional<NotificationTemplate> findById(UUID id);
    Optional<NotificationTemplate> findByKeyAndActiveTrue(String key);
    List<NotificationTemplate> findByChannelAndActiveTrue(NotificationChannel channel);
}
