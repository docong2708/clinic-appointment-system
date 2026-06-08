package com.group01.appointment.domain.entity;

import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentLogAction;
import com.group01.appointment.domain.vo.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentLog {

    private final UUID id;
    private final AppointmentId appointmentId;

    private final AppointmentLogAction action;

    private final AppointmentStatus oldStatus;
    private final AppointmentStatus newStatus;

    private final String reason;

    private final UUID performedBy;
    private final ActorRole performedByRole;

    private final LocalDateTime createdAt;

    private AppointmentLog(
            UUID id,
            AppointmentId appointmentId,
            AppointmentLogAction action,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            String reason,
            UUID performedBy,
            ActorRole performedByRole,
            LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Appointment log id must not be null");
        }

        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment id must not be null");
        }

        if (action == null) {
            throw new IllegalArgumentException("Appointment log action must not be null");
        }

        this.id = id;
        this.appointmentId = appointmentId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.performedBy = performedBy;
        this.performedByRole = performedByRole;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public static AppointmentLog create(
            AppointmentId appointmentId,
            AppointmentLogAction action,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            String reason,
            UUID performedBy,
            ActorRole performedByRole
    ) {
        return new AppointmentLog(
                UUID.randomUUID(),
                appointmentId,
                action,
                oldStatus,
                newStatus,
                reason,
                performedBy,
                performedByRole,
                LocalDateTime.now()
        );
    }

    public static AppointmentLog restore(
            UUID id,
            AppointmentId appointmentId,
            AppointmentLogAction action,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            String reason,
            UUID performedBy,
            ActorRole performedByRole,
            LocalDateTime createdAt
    ) {
        return new AppointmentLog(
                id,
                appointmentId,
                action,
                oldStatus,
                newStatus,
                reason,
                performedBy,
                performedByRole,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public AppointmentId getAppointmentId() {
        return appointmentId;
    }

    public AppointmentLogAction getAction() {
        return action;
    }

    public AppointmentStatus getOldStatus() {
        return oldStatus;
    }

    public AppointmentStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public ActorRole getPerformedByRole() {
        return performedByRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}