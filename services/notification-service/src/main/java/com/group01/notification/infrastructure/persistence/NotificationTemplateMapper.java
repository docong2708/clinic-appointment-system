package com.group01.notification.infrastructure.persistence;

import com.group01.notification.domain.aggregate.NotificationTemplate;
import com.group01.notification.domain.vo.NotificationChannel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class NotificationTemplateMapper {

    public NotificationTemplateJpaEntity toJpaEntity(NotificationTemplate domain) {
        return NotificationTemplateJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .channel(domain.getChannel().name())
                .subject(domain.getSubject())
                .body(domain.getBody())
                .version(domain.getVersion())
                .active(domain.getActive())
                .createdAt(toOffset(domain.getCreatedAt()))
                .updatedAt(toOffset(domain.getUpdatedAt()))
                .build();
    }

    public NotificationTemplate toDomain(NotificationTemplateJpaEntity entity) {
        return NotificationTemplate.restore(
                entity.getId(),
                entity.getName(),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getSubject(),
                entity.getBody(),
                entity.getVersion(),
                entity.getActive(),
                toLocal(entity.getCreatedAt()),
                toLocal(entity.getUpdatedAt())
        );
    }

    private static OffsetDateTime toOffset(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocal(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}