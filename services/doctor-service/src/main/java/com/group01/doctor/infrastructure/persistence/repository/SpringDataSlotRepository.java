package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataSlotRepository extends JpaRepository<SlotJpaEntity, UUID> {

    @Query("""
            select count(s) > 0
            from SlotJpaEntity s
            where s.doctor.id = :doctorId
              and s.status = com.group01.doctor.domain.model.SlotStatus.BOOKED
              and s.startTime < :rangeEndExclusive
              and s.endTime > :rangeStart
            """)
    boolean existsBookedSlotInRange(
            @Param("doctorId") UUID doctorId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );
}
