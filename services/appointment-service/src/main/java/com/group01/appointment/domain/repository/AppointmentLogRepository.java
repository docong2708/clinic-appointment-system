package com.group01.appointment.domain.repository;

import com.group01.appointment.domain.entity.AppointmentLog;

import java.util.List;

public interface AppointmentLogRepository {

    void saveAll(List<AppointmentLog> logs);
}
