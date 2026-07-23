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
    private DoctorId doctorId;
    private UUID slotId;
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
                AppointmentStatus.CONFIRMED,
                PaymentStatus.PENDING,
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
                AppointmentStatus.CONFIRMED,
                "Create appointment",
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

        transitionToCancelled(
                AppointmentStatus.CANCELLED,
                AppointmentLogAction.CANCEL,
                cancelReason,
                cancelledBy,
                cancelledByRole
        );
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

        if (this.status != AppointmentStatus.PENDING_DOCTOR_CONFIRMATION
                && this.status != AppointmentStatus.PENDING
                && this.status != AppointmentStatus.PENDING_PAYMENT
                && this.status != AppointmentStatus.AWAITING_PAYMENT) {
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

        transitionStatus(
                AppointmentStatus.CHECKIN_SUCCESS,
                AppointmentLogAction.CHECK_IN,
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
            throw new IllegalArgumentException("Lý do hủy không được để trống");
        }

        if (cancelledBy == null) {
            throw new IllegalArgumentException("Người hủy lịch không được để trống");
        }

        if (cancelledByRole == null) {
            throw new IllegalArgumentException("Vai trò người hủy lịch không được để trống");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the huy lich hen da qua han thanh toan");
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

    public void reschedule(
            DoctorId newDoctorId,
            UUID newSlotId,
            AppointmentTime newAppointmentTime,
            String reason,
            UUID updatedBy,
            ActorRole updatedByRole
    ) {
        if (newDoctorId == null) {
            throw new IllegalArgumentException("Mã bác sĩ mới không được để trống");
        }

        if (newSlotId == null) {
            throw new IllegalArgumentException("Mã khung giờ mới không được để trống");
        }

        if (newAppointmentTime == null) {
            throw new IllegalArgumentException("Thời gian lịch hẹn mới không được để trống");
        }

        if (updatedBy == null) {
            throw new IllegalArgumentException("Người đổi lịch không được để trống");
        }

        if (updatedByRole == null) {
            throw new IllegalArgumentException("Vai trò người đổi lịch không được để trống");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the doi lich hen da qua han thanh toan");
        }

        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Không thể đổi lịch hẹn đã hủy");
        }

        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Không thể đổi lịch hẹn đã hoàn thành");
        }

        if (newSlotId.equals(this.slotId)) {
            throw new IllegalStateException("Khung giờ mới phải khác khung giờ hiện tại");
        }

        AppointmentStatus oldStatus = this.status;
        this.doctorId = newDoctorId;
        this.slotId = newSlotId;
        this.appointmentTime = newAppointmentTime;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.RESCHEDULE,
                oldStatus,
                this.status,
                reason,
                updatedBy,
                updatedByRole
        );
    }

    public void markPaymentAwaiting(UUID performedBy, ActorRole performedByRole) {
        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Khong the cho thanh toan lich hen da huy");
        }

        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Khong the cho thanh toan lich hen da hoan thanh");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the cho thanh toan lich hen da qua han thanh toan");
        }

        if (this.paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("Lich hen da duoc thanh toan");
        }

        AppointmentStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.PENDING;
        this.status = AppointmentStatus.AWAITING_PAYMENT;
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.PAYMENT_AWAITING,
                oldStatus,
                AppointmentStatus.AWAITING_PAYMENT,
                "Pay now selected",
                performedBy,
                performedByRole
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
        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Khong the thanh toan lich hen da hoan thanh");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the thanh toan lich hen da qua han thanh toan");
        }

        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Không thể thanh toán lịch hẹn đã hủy");
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

    public void markPaymentDeferred(UUID performedBy, ActorRole performedByRole) {
        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Không thể chọn thanh toán sau cho lịch hẹn đã hủy");
        }

        if (isCompletedStatus(this.status)) {
            throw new IllegalStateException("Không thể chọn thanh toán sau cho lịch hẹn đã hoàn thành");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the chon thanh toan sau cho lich hen da qua han thanh toan");
        }

        AppointmentStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.PENDING;
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedBy = performedBy;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.PAYMENT_DEFERRED,
                oldStatus,
                AppointmentStatus.CONFIRMED,
                "Pay later selected",
                performedBy,
                performedByRole
        );
    }

    public void markPaymentFailed(UUID performedBy, ActorRole performedByRole) {
        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Không thể cập nhật thanh toán cho lịch hẹn đã hủy");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the cap nhat thanh toan cho lich hen da qua han thanh toan");
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

    public void markPaymentExpired() {
        if (this.status != AppointmentStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Chi co the qua han lich hen dang cho thanh toan");
        }

        AppointmentStatus oldStatus = this.status;

        this.paymentStatus = PaymentStatus.EXPIRED;
        this.status = AppointmentStatus.PAYMENT_EXPIRED;
        this.updatedAt = LocalDateTime.now();

        addLog(
                AppointmentLogAction.PAYMENT_EXPIRED,
                oldStatus,
                AppointmentStatus.PAYMENT_EXPIRED,
                "Payment timeout expired",
                null,
                ActorRole.SYSTEM
        );
    }

    public void complete(UUID performedBy, ActorRole performedByRole) {
        if (isCancelledStatus(this.status)) {
            throw new IllegalStateException("Không thể hoàn thành lịch hẹn đã hủy");
        }

        if (this.status == AppointmentStatus.PAYMENT_EXPIRED) {
            throw new IllegalStateException("Khong the hoan thanh lich hen da qua han thanh toan");
        }

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
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
