package com.group01.appointment.domain.repository;


import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.vo.AppointmentId;

import java.util.Optional;

public interface AppointmentRepository {

    AppointmentAggregate save(AppointmentAggregate appointmentAggregate);

    Optional<AppointmentAggregate> findById(AppointmentId appointmentId);
}