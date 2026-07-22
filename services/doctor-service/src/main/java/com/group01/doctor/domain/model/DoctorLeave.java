package com.group01.doctor.domain.model;

import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.DoctorLeaveId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DoctorLeave {
    private final DoctorLeaveId id;
    private final DoctorId doctorId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;
    private DoctorLeaveStatus status;

    public DoctorLeave(
            DoctorLeaveId id,
            DoctorId doctorId,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            DoctorLeaveStatus status
    ) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor id is required");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Leave start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Leave start date must not be after end date");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Leave reason is required");
        }
        this.id = id;
        this.doctorId = doctorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason.trim();
        this.status = status == null ? DoctorLeaveStatus.REQUESTED : status;
    }

    public static DoctorLeave request(DoctorId doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        return new DoctorLeave(DoctorLeaveId.generate(), doctorId, startDate, endDate, reason, DoctorLeaveStatus.REQUESTED);
    }

    public void cancel() {
        if (this.status == DoctorLeaveStatus.CANCELED) {
            throw new IllegalStateException("Leave request is already canceled");
        }
        this.status = DoctorLeaveStatus.CANCELED;
    }
}
