package com.group01.doctor.infrastructure.persistence.mapper;

import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.infrastructure.persistence.entity.DoctorJpaEntity;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorPersistenceMapper {
    private final SlotPersistenceMapper slotPersistenceMapper;

    public DoctorPersistenceMapper(SlotPersistenceMapper slotPersistenceMapper) {
        this.slotPersistenceMapper = slotPersistenceMapper;
    }

    public DoctorJpaEntity toJpaEntity(Doctor domain) {
        if (domain == null) return null;

        DoctorJpaEntity jpaEntity = DoctorJpaEntity.builder()
                .id(domain.getId().value())
                .userId(domain.getUserId())
                .name(domain.getName())
                .specialization(domain.getSpecialization())
                .phoneNumber(domain.getPhoneNumber())
                .email(domain.getEmail())
                .active(domain.isActive())
                .biography(domain.getBiography())
                .qualifications(domain.getQualifications())
                .avatarUrl(domain.getAvatarUrl())
                .build();

        if (domain.getSlots() != null) {
            List<SlotJpaEntity> slots = domain.getSlots().stream()
                    .map(slot -> slotPersistenceMapper.toJpaEntity(slot, jpaEntity))
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
                    .map(slotPersistenceMapper::toDomain)
                    .collect(Collectors.toList());
        }

        return new Doctor(
                DoctorId.of(entity.getId()),
                entity.getUserId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.isActive(),
                entity.getBiography(),
                entity.getQualifications(),
                entity.getAvatarUrl(),
                slots
        );
    }
}
