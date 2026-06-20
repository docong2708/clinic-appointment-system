package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.repository.NotificationRepository;
import com.group01.notification.domain.vo.NotificationId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationDeliveryJpaRepository deliveryJpaRepository;
    private final NotificationMapper mapper;

    public NotificationRepositoryAdapter(
            NotificationJpaRepository jpaRepository,
            NotificationDeliveryJpaRepository deliveryJpaRepository,
            NotificationMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.deliveryJpaRepository = deliveryJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public NotificationAggregate save(NotificationAggregate aggregate) {
        NotificationJpaEntity entity = mapper.toJpaEntity(aggregate);
        NotificationJpaEntity saved = jpaRepository.save(entity);

        List<NotificationDelivery> deliveries = aggregate.getDeliveries();
        if (!deliveries.isEmpty()) {
            deliveryJpaRepository.saveAll(mapper.deliveriesToJpa(deliveries));
        }

        NotificationAggregate result = mapper.toDomain(saved);
        List<NotificationDeliveryJpaEntity> savedDeliveries =
                deliveryJpaRepository.findByNotificationId(saved.getId());
        for (NotificationDelivery d : mapper.deliveriesToDomain(savedDeliveries)) {
            result.addDelivery(d);
        }
        return result;
    }

    @Override
    public Optional<NotificationAggregate> findById(NotificationId notificationId) {
        return jpaRepository.findById(notificationId.value())
                .map(entity -> {
                    NotificationAggregate agg = mapper.toDomain(entity);
                    List<NotificationDeliveryJpaEntity> deliveryEntities =
                            deliveryJpaRepository.findByNotificationId(entity.getId());
                    for (NotificationDelivery d : mapper.deliveriesToDomain(deliveryEntities)) {
                        agg.addDelivery(d);
                    }
                    return agg;
                });
    }

    @Override
    public List<NotificationAggregate> findByRecipientId(UUID recipientId) {
        return jpaRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<NotificationAggregate> findUnreadByRecipientId(UUID recipientId) {
        return jpaRepository.findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(recipientId)
                .stream().map(mapper::toDomain).toList();
    }
    @Override
    public void deleteById(NotificationId notificationId) {
        jpaRepository.deleteById(notificationId.value());
    }
}