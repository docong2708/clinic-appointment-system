package com.group01.appointment.infrastructure.persistence;

import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentReason;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.AppointmentTime;
import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.CancelReason;
import com.group01.appointment.domain.vo.DoctorId;
import com.group01.appointment.domain.vo.PatientId;
import com.group01.appointment.domain.vo.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class AppointmentMapper {

    public AppointmentJpaEntity toJpaEntity(AppointmentAggregate aggregate) {
        return AppointmentJpaEntity.builder()
                .id(aggregate.getAppointmentId().value())
                .patientId(aggregate.getPatientId().value())
                .doctorId(aggregate.getDoctorId().value())
                .slotId(aggregate.getSlotId())
                .rescheduledFromAppointmentId(aggregate.getRescheduledFromAppointmentId())
                .startTime(toOffsetDateTime(aggregate.getAppointmentTime().startTime()))
                .endTime(toOffsetDateTime(aggregate.getAppointmentTime().endTime()))
                .reason(aggregate.getAppointmentReason() == null
                        ? null
                        : aggregate.getAppointmentReason().value())
                .cancelReason(aggregate.getCancelReason() == null
                        ? null
                        : aggregate.getCancelReason().value())
                .status(aggregate.getStatus().name())
                .paymentStatus(aggregate.getPaymentStatus() == null
                        ? null
                        : aggregate.getPaymentStatus().name())
                .cancelledBy(aggregate.getCancelledBy())
                .cancelledByRole(aggregate.getCancelledByRole() == null
                        ? null
                        : aggregate.getCancelledByRole().name())
                .cancelledAt(toOffsetDateTime(aggregate.getCancelledAt()))
                .bookingSource(aggregate.getBookingSource())
                .createdBy(aggregate.getCreatedBy())
                .updatedBy(aggregate.getUpdatedBy())
                .confirmedAt(toOffsetDateTime(aggregate.getConfirmedAt()))
                .completedAt(toOffsetDateTime(aggregate.getCompletedAt()))
                .version(aggregate.getVersion())
                .createdAt(toOffsetDateTime(aggregate.getCreatedAt()))
                .updatedAt(toOffsetDateTime(aggregate.getUpdatedAt()))
                .build();
    }

    public AppointmentAggregate toAggregate(AppointmentJpaEntity entity) {
        return AppointmentAggregate.restore(
                AppointmentId.of(entity.getId()),
                PatientId.of(entity.getPatientId()),
                DoctorId.of(entity.getDoctorId()),
                entity.getSlotId(),
                entity.getRescheduledFromAppointmentId(),
                AppointmentTime.of(toLocalDateTime(entity.getStartTime()), toLocalDateTime(entity.getEndTime())),
                entity.getReason() == null
                        ? null
                        : AppointmentReason.of(entity.getReason()),
                entity.getCancelReason() == null
                        ? null
                        : CancelReason.of(entity.getCancelReason()),
                AppointmentStatus.valueOf(entity.getStatus()),
                entity.getPaymentStatus() == null
                        ? PaymentStatus.NOT_REQUIRED
                        : PaymentStatus.valueOf(entity.getPaymentStatus()),
                entity.getCancelledBy(),
                entity.getCancelledByRole() == null
                        ? null
                        : ActorRole.valueOf(entity.getCancelledByRole()),
                toLocalDateTime(entity.getCancelledAt()),
                entity.getBookingSource(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                toLocalDateTime(entity.getConfirmedAt()),
                toLocalDateTime(entity.getCompletedAt()),
                entity.getVersion(),
                toLocalDateTime(entity.getCreatedAt()),
                toLocalDateTime(entity.getUpdatedAt())
        );
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime value) {
        return value == null ? null : value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
