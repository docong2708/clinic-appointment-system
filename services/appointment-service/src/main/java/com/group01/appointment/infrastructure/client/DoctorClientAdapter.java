package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.DoctorServiceUnavailableException;
import com.group01.appointment.application.port.DoctorClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DoctorClientAdapter implements DoctorClientPort {

    private final DoctorServiceClient doctorServiceClient;

    public DoctorClientAdapter(DoctorServiceClient doctorServiceClient) {
        this.doctorServiceClient = doctorServiceClient;
    }

    @Override
    public boolean existsById(UUID doctorId) {
        try {
            doctorServiceClient.getDoctorById(doctorId);
            return true;
        } catch (FeignException.NotFound exception) {
            return false;
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }
}
