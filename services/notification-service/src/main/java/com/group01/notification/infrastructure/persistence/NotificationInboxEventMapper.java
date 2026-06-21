package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import com.group01.notification.domain.vo.InboxEventStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class NotificationInboxEventMapper {

    public NotificationInboxEventJpaEntity toPersistence(NotificationInboxEvent domain) {
        if (domain == null) return null;
        
        NotificationInboxEventJpaEntity entity = new NotificationInboxEventJpaEntity();
        entity.setId(domain.getId());
        entity.setSourceEventId(domain.getSourceEventId());
        entity.setSourceService(domain.getSourceService());
        entity.setEventType(domain.getEventType());
        entity.setAggregateType(domain.getAggregateType());
        entity.setAggregateId(domain.getAggregateId());
        entity.setPayload(domain.getPayload());
        entity.setStatus(domain.getStatus().name());
        entity.setCorrelationId(domain.getCorrelationId());
        entity.setErrorMessage(domain.getErrorMessage());
        
        if (domain.getReceivedAt() != null) {
            entity.setReceivedAt(domain.getReceivedAt().atOffset(ZoneOffset.UTC));
        }
        if (domain.getProcessedAt() != null) {
            entity.setProcessedAt(domain.getProcessedAt().atOffset(ZoneOffset.UTC));
        }
        
        return entity;
    }

    public NotificationInboxEvent toDomain(NotificationInboxEventJpaEntity entity) {
        if (entity == null) return null;
        
        LocalDateTime receivedAt = entity.getReceivedAt() != null 
            ? entity.getReceivedAt().toLocalDateTime() 
            : null;
        LocalDateTime processedAt = entity.getProcessedAt() != null 
            ? entity.getProcessedAt().toLocalDateTime() 
            : null;
        
        return NotificationInboxEvent.restore(
                entity.getId(),
                entity.getSourceService(),
                entity.getSourceEventId(),
                entity.getEventType(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getPayload(),
                InboxEventStatus.valueOf(entity.getStatus()),
                entity.getCorrelationId(),
                receivedAt,
                processedAt,
                entity.getErrorMessage()
        );
    }
}
