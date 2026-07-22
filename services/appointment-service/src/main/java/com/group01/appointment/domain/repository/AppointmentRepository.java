package com.group01.appointment.domain.repository;


import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.vo.AppointmentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {

    AppointmentAggregate save(AppointmentAggregate appointmentAggregate);

    Optional<AppointmentAggregate> findById(AppointmentId appointmentId);

    List<AppointmentAggregate> findDoctorAppointmentsBetween(UUID doctorId, LocalDate fromDate, LocalDate toDate);

    Optional<AppointmentAggregate> findByDoctorIdAndSlotId(UUID doctorId, UUID slotId);
}
