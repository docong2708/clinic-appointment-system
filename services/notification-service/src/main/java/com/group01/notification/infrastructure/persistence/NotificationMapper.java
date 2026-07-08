package com.group01.notification.infrastructure.persistence;

import com.group01.notification.api.dto.NotificationResponse;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.entity.NotificationDelivery;
import com.group01.notification.domain.vo.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class NotificationMapper {

    public NotificationJpaEntity toJpaEntity(NotificationAggregate agg) {
        return NotificationJpaEntity.builder()
                .id(agg.getId().value())
                .recipientUserId(agg.getRecipientId().value())
                .type(agg.getType())
                .title(agg.getTitle().value())
                .body(agg.getBody())
                .status(agg.getStatus().name())
                .priority(agg.getPriority())
                .locale(agg.getLocale())
                .sourceEventId(agg.getSourceEventId())
                .sourceService(agg.getSourceService())
                .dedupeKey(agg.getDedupeKey())
                .aggregateType(agg.getAggregateType())
                .aggregateId(agg.getAggregateId())
                .sourceInboxEventId(agg.getSourceInboxEventId())
                .templateKey(agg.getTemplateKey())
                .templateVersion(agg.getTemplateVersion())
                .actionUrl(agg.getActionUrl())
                .readAt(toOffset(agg.getReadAt()))
                .archivedAt(toOffset(agg.getArchivedAt()))
                .expiresAt(toOffset(agg.getExpiresAt()))
                .createdAt(toOffset(agg.getCreatedAt()))
                .updatedAt(toOffset(agg.getUpdatedAt()))
                .build();
    }

    public NotificationAggregate toDomain(NotificationJpaEntity entity) {
        return NotificationAggregate.restore(
                NotificationId.of(entity.getId()),
                RecipientId.of(entity.getRecipientUserId()),
                entity.getType(),
                NotificationTitle.of(entity.getTitle()),
                entity.getBody(),
                NotificationStatus.valueOf(entity.getStatus()),
                entity.getPriority(),
                entity.getLocale(),
                entity.getSourceEventId(),
                entity.getSourceService(),
                entity.getDedupeKey(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getSourceInboxEventId(),
                entity.getTemplateKey(),
                entity.getTemplateVersion(),
                entity.getActionUrl(),
                toLocal(entity.getReadAt()),
                toLocal(entity.getArchivedAt()),
                toLocal(entity.getExpiresAt()),
                toLocal(entity.getCreatedAt()),
                toLocal(entity.getUpdatedAt())
        );
    }

    public NotificationResponse toResponse(NotificationAggregate agg) {
        return NotificationResponse.builder()
                .id(agg.getId().value())
                .recipientUserId(agg.getRecipientId().value())
                .type(agg.getType())
                .title(agg.getTitle().value())
                .body(agg.getBody())
                .status(agg.getStatus().name())
                .priority(agg.getPriority())
                .createdAt(agg.getCreatedAt())
                .updatedAt(agg.getUpdatedAt())
                .build();
    }

    public NotificationDeliveryJpaEntity deliveryToJpa(NotificationDelivery delivery) {
        return NotificationDeliveryJpaEntity.builder()
                .id(delivery.getId().value())
                .notificationId(delivery.getNotificationId().value())
                .channel(delivery.getChannel().name())
                .destination(delivery.getDestination())
                .status(delivery.getStatus().name())
                .retryCount(delivery.getRetryCount())
                .scheduledAt(toOffset(delivery.getScheduledAt()))
                .nextRetryAt(toOffset(delivery.getNextRetryAt()))
                .providerName(delivery.getProviderName())
                .providerMessageId(delivery.getProviderMessageId())
                .lastError(delivery.getLastError())
                .sentAt(toOffset(delivery.getSentAt()))
                .failedAt(toOffset(delivery.getFailedAt()))
                .createdAt(toOffset(delivery.getCreatedAt()))
                .updatedAt(toOffset(delivery.getUpdatedAt()))
                .build();
    }

    public NotificationDelivery deliveryToDomain(NotificationDeliveryJpaEntity entity) {
        return NotificationDelivery.restore(
                DeliveryId.of(entity.getId()),
                NotificationId.of(entity.getNotificationId()),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getDestination(),
                DeliveryStatus.valueOf(entity.getStatus()),
                entity.getRetryCount(),
                toLocal(entity.getScheduledAt()),
                toLocal(entity.getNextRetryAt()),
                entity.getProviderName(),
                entity.getProviderMessageId(),
                entity.getLastError(),
                toLocal(entity.getSentAt()),
                toLocal(entity.getFailedAt()),
                toLocal(entity.getCreatedAt()),
                toLocal(entity.getUpdatedAt())
        );
    }

    public List<NotificationDeliveryJpaEntity> deliveriesToJpa(List<NotificationDelivery> deliveries) {
        return deliveries.stream().map(this::deliveryToJpa).toList();
    }

    public List<NotificationDelivery> deliveriesToDomain(List<NotificationDeliveryJpaEntity> entities) {
        return entities.stream().map(this::deliveryToDomain).toList();
    }

    private static OffsetDateTime toOffset(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocal(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
