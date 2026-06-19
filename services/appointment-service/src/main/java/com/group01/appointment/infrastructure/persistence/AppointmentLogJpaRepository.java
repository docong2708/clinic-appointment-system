package com.group01.appointment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentLogJpaRepository extends JpaRepository<AppointmentLogJpaEntity, UUID> {
}