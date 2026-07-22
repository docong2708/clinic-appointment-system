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
    private final UUID slotId;
    private final UUID rescheduledFromAppointmentId;

    private AppointmentTime appointmentTime;
    private AppointmentReason reason;
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

    private Appointment(
            AppointmentId id,
            PatientId patientId,
            DoctorId doctorId,
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason reason,
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
        if (id == null) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống");
        }

        if (patientId == null) {
            throw new IllegalArgumentException("Mã bệnh nhân không được để trống");
        }

        if (doctorId == null) {
            throw new IllegalArgumentException("Mã bác sĩ không được để trống");
        }

        if (appointmentTime == null) {
            throw new IllegalArgumentException("Thời gian lịch hẹn không được để trống");
        }

        if (status == null) {
            throw new IllegalArgumentException("Trạng thái lịch hẹn không được để trống");
        }

        if (paymentStatus == null) {
            throw new IllegalArgumentException("Trạng thái thanh toán không được để trống");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Thời điểm tạo không được để trống");
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException("Thời điểm cập nhật không được để trống");
        }

        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.rescheduledFromAppointmentId = rescheduledFromAppointmentId;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
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

    public static Appointment create(
            PatientId patientId,
            DoctorId doctorId,
            AppointmentTime appointmentTime,
            AppointmentReason reason
    ) {
        return create(patientId, doctorId, null, null, appointmentTime, reason, null, patientId.value());
    }

    public static Appointment create(
            PatientId patientId,
            DoctorId doctorId,
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason reason,
            String bookingSource,
            UUID createdBy
    ) {
        LocalDateTime now = LocalDateTime.now();
        UUID actorId = createdBy == null ? patientId.value() : createdBy;

        return new Appointment(
                AppointmentId.newId(),
                patientId,
                doctorId,
                slotId,
                rescheduledFromAppointmentId,
                appointmentTime,
                reason,
                null,
                AppointmentStatus.PENDING,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                bookingSource,
                actorId,
                actorId,
                now,
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
            UUID slotId,
            UUID rescheduledFromAppointmentId,
            AppointmentTime appointmentTime,
            AppointmentReason reason,
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
        return new Appointment(
                id,
                patientId,
                doctorId,
                slotId,
                rescheduledFromAppointmentId,
                appointmentTime,
                reason,
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
        if (cancelReason == null) {
            throw new IllegalArgumentException("Lý do hủy không được để trống");
        }

        if (cancelledBy == null) {
            throw new IllegalArgumentException("Người hủy lịch không được để trống");
        }

        if (cancelledByRole == null) {
            throw new IllegalArgumentException("Vai trò người hủy lịch không được để trống");
        }

        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Lịch hẹn đã được hủy trước đó");
        }

        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy lịch hẹn đã hoàn thành");
        }

        this.status = AppointmentStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledBy = cancelledBy;
        this.cancelledByRole = cancelledByRole;
        this.cancelledAt = LocalDateTime.now();
        this.updatedBy = cancelledBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentPaid() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thanh toán lịch hẹn đã hủy");
        }

        this.paymentStatus = PaymentStatus.PAID;
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaymentFailed() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật thanh toán cho lịch hẹn đã hủy");
        }

        this.paymentStatus = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Không thể hoàn thành lịch hẹn đã hủy");
        }

        if (this.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận");
        }

        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
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

    public UUID getSlotId() {
        return slotId;
    }

    public UUID getRescheduledFromAppointmentId() {
        return rescheduledFromAppointmentId;
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
}
