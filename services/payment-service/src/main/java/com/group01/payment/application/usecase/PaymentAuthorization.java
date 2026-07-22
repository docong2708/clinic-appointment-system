package com.group01.payment.application.usecase;

import com.group01.payment.domain.entity.Payment;

import java.util.UUID;

final class PaymentAuthorization {

    private PaymentAuthorization() {
    }

    static void requireOwnerOrStaff(Payment payment, UUID userId, String role) {
        if (isAdmin(role) || isDoctor(role) || payment.getPatientUserId().equals(userId)) {
            return;
        }

        throw new IllegalStateException("Bạn không có quyền truy cập thanh toán này");
    }

    static void requireOwnerOrAdmin(Payment payment, UUID userId, String role) {
        if (isAdmin(role) || payment.getPatientUserId().equals(userId)) {
            return;
        }

        throw new IllegalStateException("Bệnh nhân chỉ được thao tác thanh toán của chính mình");
    }

    static void requireDoctorOrAdmin(String role) {
        if (isAdmin(role) || isDoctor(role)) {
            return;
        }

        throw new IllegalStateException("Chỉ bác sĩ hoặc quản trị viên được xác nhận thanh toán sau tại phòng khám");
    }

    static boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }

    static boolean isDoctor(String role) {
        return "DOCTOR".equals(role);
    }
}
