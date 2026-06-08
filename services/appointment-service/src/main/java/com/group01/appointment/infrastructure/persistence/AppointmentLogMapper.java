package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.entity.AppointmentLog;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentLogAction;
import com.group01.appointment.domain.vo.AppointmentStatus;
import org.springframework.stereotype.Component;

@Component
public class AppointmentLogMapper {

    public AppointmentLogJpaEntity toJpaEntity(AppointmentLog log) {
        return AppointmentLogJpaEntity.builder()
                .id(log.getId())
                .appointmentId(log.getAppointmentId().value())
                .action(log.getAction().name())
                .oldStatus(log.getOldStatus() == null ? null : log.getOldStatus().name())
                .newStatus(log.getNewStatus() == null ? null : log.getNewStatus().name())
                .reason(log.getReason())
                .performedBy(log.getPerformedBy())
                .performedByRole(log.getPerformedByRole() == null ? null : log.getPerformedByRole().name())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public AppointmentLog toDomain(AppointmentLogJpaEntity entity) {
        return AppointmentLog.restore(
                entity.getId(),
                AppointmentId.of(entity.getAppointmentId()),
                AppointmentLogAction.valueOf(entity.getAction()),
                entity.getOldStatus() == null ? null : AppointmentStatus.valueOf(entity.getOldStatus()),
                entity.getNewStatus() == null ? null : AppointmentStatus.valueOf(entity.getNewStatus()),
                entity.getReason(),
                entity.getPerformedBy(),
                entity.getPerformedByRole() == null ? null : ActorRole.valueOf(entity.getPerformedByRole()),
                entity.getCreatedAt()
        );
    }
}