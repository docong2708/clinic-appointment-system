package com.group01.doctor.domain.model;

import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.valueobject.SlotId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Slot {
    private final SlotId id;
    private final DoctorId doctorId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private SlotStatus status;

    public Slot(SlotId id, DoctorId doctorId, LocalDateTime startTime, LocalDateTime endTime, SlotStatus status) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and End time must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before End time");
        }
        this.id = id;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : SlotStatus.AVAILABLE;
    }

    public static Slot create(DoctorId doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        return new Slot(SlotId.generate(), doctorId, startTime, endTime, SlotStatus.AVAILABLE);
    }

    public boolean overlapsWith(Slot other) {
        // Two slots overlap if: start1 < end2 AND end1 > start2
        return this.startTime.isBefore(other.getEndTime()) && this.endTime.isAfter(other.getStartTime());
    }

    public boolean isBooked() {
        return this.status == SlotStatus.BOOKED;
    }

    public void book() {
        if (this.status == SlotStatus.BOOKED) {
            throw new IllegalStateException("Slot is already booked");
        }
        this.status = SlotStatus.BOOKED;
    }

    public void reserve() {
        if (this.status != SlotStatus.AVAILABLE) {
            throw new IllegalStateException("Slot is not available to reserve");
        }
        this.status = SlotStatus.RESERVED;
    }

    public void release() {
        this.status = SlotStatus.AVAILABLE;
    }

    public void cancelBooking() {
        this.status = SlotStatus.AVAILABLE;
    }
}
