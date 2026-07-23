package com.group01.appointment.application.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DoctorClientPort {

    boolean existsById(UUID doctorId);

    DoctorProfile getDoctor(UUID doctorId);
    UUID getDoctorIdByUserId(UUID userId);

    DoctorSlot getSlot(UUID doctorId, UUID slotId);

    List<AvailableDoctorSlot> getAvailableSlots(String specialization, LocalDate date);

    AssignedDoctorSlot assignSlot(String specialization, LocalDateTime startTime, LocalDateTime endTime);

    DoctorSlot bookSlot(UUID doctorId, UUID slotId);

    void cancelSlotBooking(UUID doctorId, UUID slotId);

    void deleteSlot(UUID doctorId, UUID slotId);

    record DoctorSlot(
            UUID id,
            UUID doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked,
            String status
    ) {
    }

    record DoctorProfile(
            UUID id,
            UUID userId,
            String name,
            String specialization,
            String phoneNumber,
            String email
    ) {
    }

    record AssignedDoctorSlot(
            UUID id,
            UUID doctorId,
            UUID doctorUserId,
            String doctorName,
            String specialization,
            String doctorPhoneNumber,
            String doctorEmail,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean booked,
            String status
    ) {
        public DoctorProfile doctorProfile() {
            return new DoctorProfile(
                    doctorId,
                    doctorUserId,
                    doctorName,
                    specialization,
                    doctorPhoneNumber,
                    doctorEmail
            );
        }
    }

    record AvailableDoctorSlot(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long availableCount
    ) {
    }
}
