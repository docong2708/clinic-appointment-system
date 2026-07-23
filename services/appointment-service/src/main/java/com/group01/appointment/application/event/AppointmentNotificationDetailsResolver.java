package com.group01.appointment.application.event;

import com.group01.appointment.application.port.DoctorClientPort;
import com.group01.appointment.application.port.PatientClientPort;
import com.group01.appointment.application.port.UserClientPort;
import com.group01.appointment.domain.aggregate.AppointmentAggregate;
import org.springframework.stereotype.Service;

@Service
public class AppointmentNotificationDetailsResolver {

    private final PatientClientPort patientClientPort;
    private final UserClientPort userClientPort;
    private final DoctorClientPort doctorClientPort;

    public AppointmentNotificationDetailsResolver(
            PatientClientPort patientClientPort,
            UserClientPort userClientPort,
            DoctorClientPort doctorClientPort
    ) {
        this.patientClientPort = patientClientPort;
        this.userClientPort = userClientPort;
        this.doctorClientPort = doctorClientPort;
    }

    public AppointmentNotificationDetails resolve(AppointmentAggregate appointment) {
        DoctorClientPort.DoctorProfile doctor = doctorClientPort.getDoctor(appointment.getDoctorId().value());
        return resolve(appointment, doctor);
    }

    public AppointmentNotificationDetails resolve(
            AppointmentAggregate appointment,
            DoctorClientPort.DoctorProfile doctor
    ) {
        PatientClientPort.PatientProfile patient = patientClientPort.getPatient(appointment.getPatientId().value());
        if (patient.userId() == null) {
            throw new IllegalStateException("Missing patient user id for appointment notification");
        }

        UserClientPort.UserProfile patientUser = userClientPort.getUser(patient.userId());
        if (!hasText(patientUser.email())) {
            throw new IllegalStateException("Missing patient email for appointment notification");
        }

        return new AppointmentNotificationDetails(
                patient.userId(),
                patientUser.email(),
                doctor.name(),
                doctor.specialization()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
