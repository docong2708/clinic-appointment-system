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
    private final UUID slotId;
    private final UUID rescheduledFromAppointmentId;

    private AppointmentTime appointmentTime;
    private AppointmentReason appointmentReason;
    private CancelReason cancelReason;

    private AppointmentStatus status;
    private PaymentStatus paymentStatus;

    private UUID cancelledBy;
    private ActorRole cancelledByRole;
    private LocalDateTime cancelledAt;
    private String bookingSource;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private Integer version;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<AppointmentLog> logs = new ArrayList<>();

    private AppointmentAggregate(
            AppointmentId appointmentId,
            PatientId patientId,
            DoctorId doctorId,
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            String bookingSource,
            UUID createdBy,
            UUID updatedBy,
            LocalDateTime confirmedAt,
            LocalDateTime completedAt,
            Integer version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.rescheduledFromAppointmentId = rescheduledFromAppointmentId;
        this.appointmentTime = appointmentTime;
        this.appointmentReason = appointmentReason;
        this.cancelReason = cancelReason;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = cancelledAt;
        this.bookingSource = bookingSource;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.confirmedAt = confirmedAt;
        this.completedAt = completedAt;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentAggregate create(
            PatientId patientId,
            DoctorId doctorId,
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason,
            String bookingSource,
            UUID createdBy
    ) {
        LocalDateTime now = LocalDateTime.now();
        UUID actorId = createdBy == null ? patientId.value() : createdBy;

        AppointmentAggregate aggregate = new AppointmentAggregate(
                AppointmentId.newId(),
                patientId,
                doctorId,
                slotId,
                rescheduledFromAppointmentId,
                appointmentTime,
                appointmentReason,
                null,
                AppointmentStatus.PENDING_DOCTOR_CONFIRMATION,
                PaymentStatus.NOT_REQUIRED,
                null,
                null,
                null,
                bookingSource,
                actorId,
                actorId,
                null,
                null,
                null,
                now,
                now
        );

        aggregate.addLog(
                AppointmentLogAction.CREATE,
                null,
                AppointmentStatus.PENDING_DOCTOR_CONFIRMATION,
                "Create appointment waiting for doctor confirmation",
                actorId,
                ActorRole.PATIENT
        );

        return aggregate;
    }

    public static AppointmentAggregate restore(
            AppointmentId appointmentId,
            PatientId patientId,
            DoctorId doctorId,
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason appointmentReason,
            CancelReason cancelReason,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            UUID cancelledBy,
            ActorRole cancelledByRole,
            LocalDateTime cancelledAt,
            String bookingSource,
            UUID createdBy,
            UUID updatedBy,
            LocalDateTime confirmedAt,
            LocalDateTime completedAt,
            Integer version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AppointmentAggregate(
                appointmentId,
                patientId,
                doctorId,
                slotId,
                rescheduledFromAppointmentId,
                appointmentTime,
                appointmentReason,
                cancelReason,
                status,
                paymentStatus,
                cancelledBy,
                cancelledByRole,
                cancelledAt,
                bookingSource,
                createdBy,
                updatedBy,
                confirmedAt,
                completedAt,
                version,
                createdAt,
                updatedAt
        );
    }

    public void cancel(
            CancelReason cancelReason,
            UUID cancelledBy,
            ActorRole cancelledByRole
    ) {
        validateCancellationArguments(cancelReason, cancelledBy, cancelledByRole);

        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Completed appointment cannot be cancelled");
        }

        transitionToCancelled(AppointmentStatus.CANCELLED, AppointmentLogAction.CANCEL, cancelReason, cancelledBy, cancelledByRole);
    }

    public void cancelByDoctor(
            CancelReason cancelReason,
            UUID cancelledBy
    ) {
        validateCancellationArguments(cancelReason, cancelledBy, ActorRole.DOCTOR);

        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Completed appointment cannot be cancelled");
        }

        transitionToCancelled(
                AppointmentStatus.CANCELLED_BY_DOCTOR,
                AppointmentLogAction.DOCTOR_CANCEL,
                cancelReason,
                cancelledBy,
                ActorRole.DOCTOR
        );
    }

    public void validateNotStarted(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time must not be null");
        }

        if (!currentTime.isBefore(this.appointmentTime.startTime())) {
            throw new IllegalStateException("Appointment slot is already in the past or has started");
        }
    }

    public boolean canDoctorCancel(LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("Current time must not be null");
        }

        return !currentTime.isAfter(this.appointmentTime.startTime().minusHours(5));
    }

    public void confirmByDoctor(UUID performedBy) {
        validateActor(performedBy, ActorRole.DOCTOR);

        if (this.status != AppointmentStatus.PENDING_DOCTOR_CONFIRMATION) {
            throw new IllegalStateException("Only doctor-pending appointment can be confirmed");
        }

        AppointmentStatus oldStatus = this.status;
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.DOCTOR_CONFIRM,
                oldStatus,
                AppointmentStatus.CONFIRMED,
                "Doctor confirmed appointment",
                performedBy,
                ActorRole.DOCTOR
        );
    }

    public void markNotCheckIn(UUID performedBy, ActorRole performedByRole, LocalDateTime currentTime) {
        validateActor(performedBy, performedByRole);

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointment can be marked as not check-in");
        }

        if (currentTime == null) {
            throw new IllegalArgumentException("Current time must not be null");
        }

        if (currentTime.isBefore(this.appointmentTime.startTime())) {
            throw new IllegalStateException("Cannot mark not check-in before appointment start time");
        }

        transitionStatus(
                AppointmentStatus.NOT_CHECKIN,
                AppointmentLogAction.MARK_NOT_CHECKIN,
                "Patient did not arrive at the clinic",
                performedBy,
                performedByRole
        );
    }

    public void checkIn(UUID performedBy, ActorRole performedByRole, LocalDateTime currentTime) {
        validateActor(performedBy, performedByRole);

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointment can be checked in");
        }

        if (currentTime == null) {
            throw new IllegalArgumentException("Current time must not be null");
        }

        if (currentTime.isBefore(this.appointmentTime.startTime())) {
            throw new IllegalStateException("Cannot check in before appointment start time");
        }

        LocalDateTime checkInDeadline = this.appointmentTime.startTime().plusMinutes(10);
        if (currentTime.isAfter(checkInDeadline)) {
            throw new IllegalStateException("Cannot check in after the first 10 minutes from appointment start time");
        }

        AppointmentStatus oldStatus = this.status;

        this.status = AppointmentStatus.CHECKIN_SUCCESS;
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.CHECK_IN,
                oldStatus,
                AppointmentStatus.CHECKIN_SUCCESS,
                "Doctor started consultation",
                performedBy,
                performedByRole
        );
    }

    public void checkout(UUID performedBy, ActorRole performedByRole) {
        validateActor(performedBy, performedByRole);

        if (this.status != AppointmentStatus.CHECKIN_SUCCESS) {
            throw new IllegalStateException("Only checked-in appointment can be checked out");
        }

        AppointmentStatus oldStatus = this.status;

        this.status = AppointmentStatus.CHECKOUT_SUCCESS;
        this.completedAt = LocalDateTime.now();
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.CHECK_OUT,
                oldStatus,
                AppointmentStatus.CHECKOUT_SUCCESS,
                "Doctor completed consultation and medical record was saved",
                performedBy,
                performedByRole
        );
    }

    private void validateCancellationArguments(
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
    }

    private void validateActor(UUID performedBy, ActorRole performedByRole) {
        if (performedBy == null) {
            throw new IllegalArgumentException("Performed by must not be null");
        }

        if (performedByRole == null) {
            throw new IllegalArgumentException("Performed by role must not be null");
        }
    }

    private void transitionToCancelled(
            AppointmentStatus cancelledStatus,
            AppointmentLogAction logAction,
            CancelReason cancelReason,
            UUID cancelledBy,
            ActorRole cancelledByRole
    ) {
        AppointmentStatus oldStatus = this.status;

        this.status = cancelledStatus;
        this.cancelReason = cancelReason;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = LocalDateTime.now();
        this.updatedBy = cancelledBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                logAction,
                oldStatus,
                cancelledStatus,
                cancelReason.value(),
                cancelledBy,
                cancelledByRole
        );
    }

    private void transitionStatus(
            AppointmentStatus newStatus,
            AppointmentLogAction logAction,
            String reason,
            UUID performedBy,
            ActorRole performedByRole
    ) {
        AppointmentStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(logAction, oldStatus, newStatus, reason, performedBy, performedByRole);
    }

    private boolean isCancelledStatus(AppointmentStatus status) {
        return status == AppointmentStatus.CANCELLED || status == AppointmentStatus.CANCELLED_BY_DOCTOR;
    }

    private boolean isCompletedStatus(AppointmentStatus status) {
        return status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CHECKOUT_SUCCESS;
    }

    public void markPaymentPaid(UUID performedBy, ActorRole performedByRole) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled appointment cannot be paid");
        }

        AppointmentStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.PAID;
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedBy = performedBy;
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
        this.updatedBy = performedBy;
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
        this.completedAt = LocalDateTime.now();
        this.updatedBy = performedBy;
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

    public UUID getSlotId() {
        return slotId;
    }

    public UUID getRescheduledFromAppointmentId() {
        return rescheduledFromAppointmentId;
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

    public String getBookingSource() {
        return bookingSource;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Integer getVersion() {
        return version;
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
