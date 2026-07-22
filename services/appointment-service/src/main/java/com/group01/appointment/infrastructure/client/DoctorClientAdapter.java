package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.DoctorServiceUnavailableException;
import com.group01.appointment.application.exception.SlotNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public UUID getDoctorIdByUserId(UUID userId) {
        try {
            return doctorServiceClient.getDoctorByUserId(userId).doctorId();
        } catch (FeignException.NotFound exception) {
            throw new com.group01.appointment.application.exception.DoctorNotFoundException(userId);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    @Override
    public DoctorSlot getSlot(UUID doctorId, UUID slotId) {
        try {
            List<DoctorServiceClient.DoctorSlotResponse> slots = doctorServiceClient.getSlots(doctorId);
            return slots.stream()
                    .filter(slot -> slot.id().equals(slotId))
                    .findFirst()
                    .map(this::toDoctorSlot)
                    .orElseThrow(() -> new SlotNotFoundException(doctorId, slotId));
        } catch (FeignException.NotFound exception) {
            return throwDoctorNotFound(doctorId);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    @Override
    public DoctorSlot bookSlot(UUID doctorId, UUID slotId) {
        try {
            return toDoctorSlot(doctorServiceClient.bookSlot(doctorId, slotId));
        } catch (FeignException.NotFound exception) {
            return throwDoctorNotFound(doctorId);
        } catch (FeignException.BadRequest exception) {
            throw new IllegalStateException("Slot cannot be booked: " + slotId, exception);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    @Override
    public void cancelSlotBooking(UUID doctorId, UUID slotId) {
        try {
            doctorServiceClient.cancelSlotBooking(doctorId, slotId);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    @Override
    public void deleteSlot(UUID doctorId, UUID slotId) {
        try {
            doctorServiceClient.deleteSlot(doctorId, slotId);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    private DoctorSlot toDoctorSlot(DoctorServiceClient.DoctorSlotResponse response) {
        return new DoctorSlot(
                response.id(),
                response.doctorId(),
                response.startTime(),
                response.endTime(),
                response.booked()
        );
    }

    private DoctorSlot throwDoctorNotFound(UUID doctorId) {
        throw new com.group01.appointment.application.exception.DoctorNotFoundException(doctorId);
    }
}
