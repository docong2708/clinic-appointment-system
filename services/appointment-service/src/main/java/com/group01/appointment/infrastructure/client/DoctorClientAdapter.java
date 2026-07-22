package com.group01.appointment.infrastructure.client;

import com.group01.appointment.application.exception.DoctorServiceUnavailableException;
import com.group01.appointment.application.exception.SlotNotFoundException;
import com.group01.appointment.application.port.DoctorClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public DoctorProfile getDoctor(UUID doctorId) {
        try {
            return toDoctorProfile(doctorServiceClient.getDoctorById(doctorId));
        } catch (FeignException.NotFound exception) {
            throw new com.group01.appointment.application.exception.DoctorNotFoundException(doctorId);
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
    public List<AvailableDoctorSlot> getAvailableSlots(String specialization, LocalDate date) {
        try {
            return doctorServiceClient.getAvailableSlots(specialization, date.toString())
                    .stream()
                    .map(this::toAvailableDoctorSlot)
                    .toList();
        } catch (FeignException.BadRequest exception) {
            throw new IllegalStateException("Không thể lấy khung giờ trống theo chuyên khoa", exception);
        } catch (FeignException exception) {
            throw new DoctorServiceUnavailableException(exception);
        }
    }

    @Override
    public AssignedDoctorSlot assignSlot(String specialization, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return toAssignedDoctorSlot(doctorServiceClient.assignSlot(new DoctorServiceClient.AssignSlotRequest(
                    specialization,
                    startTime,
                    endTime
            )));
        } catch (FeignException.BadRequest exception) {
            throw new IllegalStateException("Không thể gán bác sĩ cho chuyên khoa và khung giờ đã chọn", exception);
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
            throw new IllegalStateException("Không thể đặt khung giờ: " + slotId, exception);
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

    private DoctorSlot toDoctorSlot(DoctorServiceClient.DoctorSlotResponse response) {
        return new DoctorSlot(
                response.id(),
                response.doctorId(),
                response.startTime(),
                response.endTime(),
                response.booked(),
                response.status()
        );
    }

    private AvailableDoctorSlot toAvailableDoctorSlot(DoctorServiceClient.AvailableSlotResponse response) {
        return new AvailableDoctorSlot(
                response.startTime(),
                response.endTime(),
                response.availableCount()
        );
    }

    private AssignedDoctorSlot toAssignedDoctorSlot(DoctorServiceClient.AssignedSlotResponse response) {
        return new AssignedDoctorSlot(
                response.id(),
                response.doctorId(),
                response.doctorUserId(),
                response.doctorName(),
                response.specialization(),
                response.doctorPhoneNumber(),
                response.doctorEmail(),
                response.startTime(),
                response.endTime(),
                response.booked(),
                response.status()
        );
    }

    private DoctorProfile toDoctorProfile(DoctorServiceClient.DoctorResponse response) {
        return new DoctorProfile(
                response.id(),
                response.userId(),
                response.name(),
                response.specialization(),
                response.phoneNumber(),
                response.email()
        );
    }

    private DoctorSlot throwDoctorNotFound(UUID doctorId) {
        throw new com.group01.appointment.application.exception.DoctorNotFoundException(doctorId);
    }
}
