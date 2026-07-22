package com.group01.appointment.domain.repository;


import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.PatientId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {

    AppointmentAggregate save(AppointmentAggregate appointmentAggregate);

    Optional<AppointmentAggregate> findById(AppointmentId appointmentId);

    List<AppointmentAggregate> findByPatientId(PatientId patientId);

    List<AppointmentAggregate> findAwaitingPaymentUpdatedBefore(LocalDateTime updatedBefore, int limit);
}
