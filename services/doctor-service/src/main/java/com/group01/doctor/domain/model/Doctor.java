package com.group01.doctor.domain.model;

import com.group01.doctor.domain.valueobject.DoctorId;
import com.group01.doctor.domain.exception.SlotOverlapException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Doctor {
    private final DoctorId id;
    private final UUID userId;
    private String name;
    private String specialization;
    private String phoneNumber;
    private String email;
    private boolean active;
    private String biography;
    private String qualifications;
    private String avatarUrl;
    private final List<Slot> slots;

    public Doctor(DoctorId id, UUID userId, String name, String specialization, String phoneNumber, String email, boolean active, String biography, String qualifications, String avatarUrl, List<Slot> slots) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.active = active;
        this.biography = biography;
        this.qualifications = qualifications;
        this.avatarUrl = avatarUrl;
        this.slots = slots != null ? new ArrayList<>(slots) : new ArrayList<>();
    }

    public static Doctor create(UUID userId, String name, String specialization, String phoneNumber, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return new Doctor(DoctorId.generate(), userId, name, specialization, phoneNumber, email, true, null, null, null, new ArrayList<>());
    }

    public void updateDetails(String name, String specialization, String phoneNumber, String email, boolean active) {
        this.name = name;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.active = active;
    }

    public void updateProfile(String name, String specialization, String phoneNumber, String email, String biography, String qualifications, String avatarUrl) {
        this.name = name;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.biography = biography;
        this.qualifications = qualifications;
        this.avatarUrl = avatarUrl;
    }

    public void addSlot(Slot newSlot) {
        if (!this.active) {
            throw new IllegalStateException("Cannot add a slot to an inactive doctor");
        }
        for (Slot existingSlot : slots) {
            if (existingSlot.overlapsWith(newSlot)) {
                throw new SlotOverlapException("New slot [" + newSlot.getStartTime() + " - " + newSlot.getEndTime() + 
                                               "] overlaps with an existing slot [" + existingSlot.getStartTime() + " - " + existingSlot.getEndTime() + "]");
            }
        }
        this.slots.add(newSlot);
    }

    public void removeSlot(Slot slot) {
        this.slots.removeIf(s -> s.getId().equals(slot.getId()));
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
