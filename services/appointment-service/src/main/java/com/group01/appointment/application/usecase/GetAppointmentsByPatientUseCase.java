package com.group01.appointment.application.usecase;

import com.group01.appointment.application.result.AppointmentResult;
import com.group01.appointment.application.result.AppointmentResultMapper;
import com.group01.appointment.domain.repository.AppointmentRepository;
import com.group01.appointment.domain.vo.PatientId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetAppointmentsByPatientUseCase {

    private final AppointmentRepository appointmentRepository;

    public GetAppointmentsByPatientUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResult> execute(UUID patientId) {
        return appointmentRepository.findByPatientId(PatientId.of(patientId))
                .stream()
                .map(AppointmentResultMapper::from)
                .toList();
    }
}
