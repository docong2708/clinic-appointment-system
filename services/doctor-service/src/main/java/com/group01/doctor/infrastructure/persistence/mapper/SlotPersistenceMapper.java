package com.group01.doctor.infrastructure.persistence.mapper;

import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.SlotId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SlotPersistenceMapper {

    public Slot toDomain(SlotJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Slot(
                SlotId.of(entity.getId()),
                DoctorId.of(entity.getDoctor().getId()),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus()
        );
    }

    public SlotJpaEntity toJpaEntity(Slot slot, DoctorJpaEntity doctorEntity) {
        if (slot == null) {
            return null;
        }
        return SlotJpaEntity.builder()
                .id(slot.getId().value())
                .doctor(doctorEntity)
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .build();
    }
}
