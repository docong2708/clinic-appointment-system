package com.group01.appointment.domain.aggregate;

import com.group01.appointment.domain.entity.AppointmentLog;
import com.group01.appointment.domain.vo.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentAggregate {

    private final AppointmentId appointmentId;
    private final PatientId patientId;
    private final DoctorId doctorId;

    private AppointmentTime appointmentTime;
    private AppointmentReason appointmentReason;
    private CancelReason cancelReason;

    private AppointmentStatus status;
    private PaymentStatus paymentStatus;

    private UUID cancelledBy;
    private ActorRole cancelledByRole;
    private LocalDateTime cancelledAt;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<AppointmentLog> logs = new ArrayList<>();

    private AppointmentAggregate(
            AppointmentId appointmentId,
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.appointmentReason = appointmentReason;
        this.cancelReason = cancelReason;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentAggregate create(
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason
    ) {
        LocalDateTime now = LocalDateTime.now();

        AppointmentAggregate aggregate = new AppointmentAggregate(
                AppointmentId.newId(),
                patientId,
                doctorId,
                appointmentTime,
                appointmentReason,
                null,
                AppointmentStatus.PENDING_PAYMENT,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                now,
                now
        );

        aggregate.addLog(
                AppointmentLogAction.CREATE,
                null,
                AppointmentStatus.PENDING_PAYMENT,
                "Create appointment",
                patientId.value(),
                ActorRole.PATIENT
        );

        return aggregate;
    }

    public static AppointmentAggregate restore(
            AppointmentId appointmentId,
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AppointmentAggregate(
                appointmentId,
                patientId,
                doctorId,
                appointmentTime,
                appointmentReason,
                cancelReason,
                status,
                paymentStatus,
                cancelledBy,
                cancelledByRole,
                cancelledAt,
                createdAt,
                updatedAt
        );
    }

    public void cancel(
            CancelReason cancelReason,
            UUID cancelledBy,
            ActorRole cancelledByRole
    ) {
        if (cancelReason == null) {
            throw new IllegalArgumentException("Cancel reason must not be null");
        }

        if (cancelledBy == null) {
            throw new IllegalArgumentException("Cancelled by must not be null");
        }

        if (cancelledByRole == null) {
            throw new IllegalArgumentException("Cancelled by role must not be null");
        }

        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Completed appointment cannot be cancelled");
        }

        AppointmentStatus oldStatus = this.status;

        this.status = AppointmentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.CANCEL,
                oldStatus,
                AppointmentStatus.CANCELLED,
                cancelReason.value(),
                cancelledBy,
                cancelledByRole
        );
    }

    public void markPaymentPaid(UUID performedBy, ActorRole performedByRole) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be paid");
        }

        AppointmentStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.PAID;
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.PAYMENT_SUCCESS,
                oldStatus,
                AppointmentStatus.CONFIRMED,
                "Payment completed",
                performedBy,
                performedByRole
        );
    }

    public void markPaymentFailed(UUID performedBy, ActorRole performedByRole) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot update payment");
        }

        this.paymentStatus = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.PAYMENT_FAILED,
                this.status,
                this.status,
                "Payment failed",
                performedBy,
                performedByRole
        );
    }

    public void complete(UUID performedBy, ActorRole performedByRole) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be completed");
        }

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointment can be completed");
        }

        AppointmentStatus oldStatus = this.status;

        this.status = AppointmentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.COMPLETE,
                oldStatus,
                AppointmentStatus.COMPLETED,
                "Complete appointment",
                performedBy,
                performedByRole
        );
    }

    private void addLog(
            AppointmentLogAction action,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            String reason,
            UUID performedBy,
            ActorRole performedByRole
    ) {
        AppointmentLog log = AppointmentLog.create(
                this.appointmentId,
                action,
                oldStatus,
                newStatus,
                reason,
                performedBy,
                performedByRole
        );

        this.logs.add(log);
    }

    public AppointmentId getAppointmentId() {
        return appointmentId;
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public DoctorId getDoctorId() {
        return doctorId;
    }

    public AppointmentTime getAppointmentTime() {
        return appointmentTime;
    }

    public AppointmentReason getAppointmentReason() {
        return appointmentReason;
    }

    public CancelReason getCancelReason() {
        return cancelReason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public UUID getCancelledBy() {
        return cancelledBy;
    }

    public ActorRole getCancelledByRole() {
        return cancelledByRole;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<AppointmentLog> getLogs() {
        return List.copyOf(logs);
    }
}
