package com.group01.appointment.domain.entity;

import com.group01.appointment.domain.vo.ActorRole;
import com.group01.appointment.domain.vo.AppointmentId;
import com.group01.appointment.domain.vo.AppointmentReason;
import com.group01.appointment.domain.vo.AppointmentStatus;
import com.group01.appointment.domain.vo.AppointmentTime;
import com.group01.appointment.domain.vo.CancelReason;
import com.group01.appointment.domain.vo.DoctorId;
import com.group01.appointment.domain.vo.PatientId;
import com.group01.appointment.domain.vo.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {

    private final AppointmentId id;
    private final PatientId patientId;
    private final DoctorId doctorId;

    private AppointmentTime appointmentTime;
    private AppointmentReason reason;
    private CancelReason cancelReason;

    private AppointmentStatus status;
    private PaymentStatus paymentStatus;

    private UUID cancelledBy;
    private ActorRole cancelledByRole;
    private LocalDateTime cancelledAt;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Appointment(
            AppointmentId id,
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason reason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Appointment id must not be null");
        }

        if (patientId == null) {
            throw new IllegalArgumentException("Patient id must not be null");
        }

        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor id must not be null");
        }

        if (appointmentTime == null) {
            throw new IllegalArgumentException("Appointment time must not be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("Appointment status must not be null");
        }

        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status must not be null");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Created at must not be null");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("Updated at must not be null");
        }

        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.cancelReason = cancelReason;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Appointment create(
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason reason
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Appointment(
                AppointmentId.newId(),
                patientId,
                doctorId,
                appointmentTime,
                reason,
                null,
                AppointmentStatus.PENDING_PAYMENT,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                now,
                now
        );
    }

    public static Appointment restore(
            AppointmentId id,
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason reason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Appointment(
                id,
                patientId,
                doctorId,
                appointmentTime,
                reason,
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

        this.status = AppointmentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentPaid() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be paid");
        }

        this.paymentStatus = PaymentStatus.PAID;
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentFailed() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot update payment");
        }

        this.paymentStatus = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be completed");
        }

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointment can be completed");
        }

        this.status = AppointmentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public AppointmentId getId() {
        return id;
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

    public AppointmentReason getReason() {
        return reason;
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
}