package com.group01.doctor.infrastructure.persistence.mapper;

import com.group01.doctor.domain.model.DoctorLeave;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.DoctorLeaveId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.DoctorLeaveJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorLeavePersistenceMapper {

    public DoctorLeave toDomain(DoctorLeaveJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DoctorLeave(
                DoctorLeaveId.of(entity.getId()),
                DoctorId.of(entity.getDoctor().getId()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReason(),
                entity.getStatus()
        );
    }

    public DoctorLeaveJpaEntity toJpaEntity(DoctorLeave doctorLeave, DoctorJpaEntity doctorEntity) {
        if (doctorLeave == null) {
            return null;
        }
        return DoctorLeaveJpaEntity.builder()
                .id(doctorLeave.getId().value())
                .doctor(doctorEntity)
                .startDate(doctorLeave.getStartDate())
                .endDate(doctorLeave.getEndDate())
                .reason(doctorLeave.getReason())
                .status(doctorLeave.getStatus())
                .build();
    }
}
