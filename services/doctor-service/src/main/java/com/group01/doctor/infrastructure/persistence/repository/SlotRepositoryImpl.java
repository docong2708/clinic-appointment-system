package com.group01.doctor.infrastructure.persistence.repository;

import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.SlotRepository;
import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.infrastructure.persistence.entity.SlotJpaEntity;
import com.group01.doctor.infrastructure.persistence.mapper.SlotPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotRepositoryImpl implements SlotRepository {

    private final SpringDataSlotRepository springDataSlotRepository;
    private final SlotPersistenceMapper slotPersistenceMapper;
    private final EntityManager entityManager;

    @Override
    public List<Slot> findByDoctorIdAndFilters(DoctorId doctorId, LocalDateTime fromDate, LocalDateTime toDate, SlotStatus status) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SlotJpaEntity> criteriaQuery = criteriaBuilder.createQuery(SlotJpaEntity.class);
        Root<SlotJpaEntity> slotRoot = criteriaQuery.from(SlotJpaEntity.class);
        Join<Object, Object> doctorJoin = slotRoot.join("doctor", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(doctorJoin.get("id"), doctorId.value()));
        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(slotRoot.get("startTime"), fromDate));
        }
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(slotRoot.get("endTime"), toDate));
        }
        if (status != null) {
            predicates.add(criteriaBuilder.equal(slotRoot.get("status"), status));
        }

        criteriaQuery.select(slotRoot)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(criteriaBuilder.asc(slotRoot.get("startTime")));

        TypedQuery<SlotJpaEntity> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList()
                .stream()
                .map(slotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBookedSlotInRange(DoctorId doctorId, LocalDateTime rangeStart, LocalDateTime rangeEndExclusive) {
        return springDataSlotRepository.existsBookedSlotInRange(doctorId.value(), rangeStart, rangeEndExclusive);
    }
}
