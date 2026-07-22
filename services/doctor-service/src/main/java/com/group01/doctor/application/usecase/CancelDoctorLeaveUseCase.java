package com.group01.doctor.application.usecase;

import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.exception.DomainException;
import com.group01.doctor.domain.model.Doctor;
import com.group01.doctor.domain.model.DoctorLeave;
import com.group01.doctor.domain.model.Slot;
import com.group01.doctor.domain.model.SlotStatus;
import com.group01.doctor.domain.repository.DoctorLeaveRepository;
import com.group01.doctor.domain.repository.DoctorRepository;
import com.group01.doctor.domain.valueobject.DoctorLeaveId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelDoctorLeaveUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;

    @Transactional
    public void execute(UUID userId, UUID leaveId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor profile not found for user ID " + userId));

        DoctorLeave doctorLeave = doctorLeaveRepository.findById(DoctorLeaveId.of(leaveId))
                .orElseThrow(() -> new DomainException("Doctor leave with ID " + leaveId + " not found"));

        if (!doctorLeave.getDoctorId().equals(doctor.getId())) {
            throw new DomainException("You do not have permission to cancel this leave request");
        }

        LocalDateTime rangeStart = doctorLeave.getStartDate().atStartOfDay();
        LocalDateTime rangeEndExclusive = doctorLeave.getEndDate().plusDays(1).atStartOfDay();

        for (Slot slot : doctor.getSlots()) {
            if (slot.getStatus() == SlotStatus.BLOCKED
                    && slot.getStartTime().isBefore(rangeEndExclusive)
                    && slot.getEndTime().isAfter(rangeStart)) {
                slot.makeAvailable();
            }
        }

        doctorLeave.cancel();
        doctorRepository.save(doctor);
        doctorLeaveRepository.save(doctorLeave);

        log.info("Doctor leave canceled doctorId={} leaveId={}", doctor.getId().value(), leaveId);
    }
}
