package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import com.group01.notification.domain.repository.NotificationInboxEventRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Component
public class NotificationInboxEventRepositoryAdapter implements NotificationInboxEventRepository {

    private final NotificationInboxEventJpaRepository jpaRepository;
    private final NotificationInboxEventMapper mapper;

    public NotificationInboxEventRepositoryAdapter(
            NotificationInboxEventJpaRepository jpaRepository,
            NotificationInboxEventMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public NotificationInboxEvent save(NotificationInboxEvent event) {
        NotificationInboxEventJpaEntity entity = mapper.toPersistence(event);
        NotificationInboxEventJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationInboxEvent> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsBySourceEventId(UUID sourceEventId) {
        List<NotificationInboxEventJpaEntity> results = 
                jpaRepository.findBySourceServiceAndSourceEventId("appointment-service", sourceEventId);
        return !results.isEmpty();
    }
}
