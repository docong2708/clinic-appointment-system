package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.repository.NotificationTemplateRepository;
import com.group01.notification.domain.vo.NotificationChannel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationTemplateRepositoryAdapter implements NotificationTemplateRepository {

    private final NotificationTemplateJpaRepository jpaRepository;
    private final NotificationTemplateMapper mapper;

    public NotificationTemplateRepositoryAdapter(
            NotificationTemplateJpaRepository jpaRepository,
            NotificationTemplateMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(NotificationTemplate template) {
        jpaRepository.save(mapper.toJpaEntity(template));
    }

    @Override
    public Optional<NotificationTemplate> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<NotificationTemplate> findByKeyAndActiveTrue(String key) {
        return jpaRepository.findByNameAndActiveTrue(key).map(mapper::toDomain);
    }

    @Override
    public List<NotificationTemplate> findByChannelAndActiveTrue(NotificationChannel channel) {
        return jpaRepository.findByChannelAndActiveTrue(channel.name())
                .stream().map(mapper::toDomain).toList();
    }
}