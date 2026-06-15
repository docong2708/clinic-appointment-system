package com.group01.doctor.infrastructure.persistence.mapper;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.SlotId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorPersistenceMapper {

    public DoctorJpaEntity toJpaEntity(Doctor domain) {
        if (domain == null) return null;

        DoctorJpaEntity jpaEntity = DoctorJpaEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .specialization(domain.getSpecialization())
                .phoneNumber(domain.getPhoneNumber())
                .email(domain.getEmail())
                .active(domain.isActive())
                .build();

        if (domain.getSlots() != null) {
            List<SlotJpaEntity> slots = domain.getSlots().stream()
                    .map(slot -> SlotJpaEntity.builder()
                            .id(slot.getId().value())
                            .doctor(jpaEntity)
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .booked(slot.isBooked())
                            .build())
                    .collect(Collectors.toList());
            jpaEntity.setSlots(slots);
        }
        return jpaEntity;
    }

    public Doctor toDomain(DoctorJpaEntity entity) {
        if (entity == null) return null;

        List<Slot> slots = new ArrayList<>();
        if (entity.getSlots() != null) {
            slots = entity.getSlots().stream()
                    .map(slotEntity -> new Slot(
                            SlotId.of(slotEntity.getId()),
                            DoctorId.of(slotEntity.getDoctor().getId()),
                            slotEntity.getStartTime(),
                            slotEntity.getEndTime(),
                            slotEntity.isBooked()
                    ))
                    .collect(Collectors.toList());
        }

        return new Doctor(
                DoctorId.of(entity.getId()),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.isActive(),
                slots
        );
    }
}
