package com.group01.appointment.domain.vo;

public record CancelReason(String value) {

    private static final int MAX_LENGTH = 500;

    public CancelReason {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lý do hủy không được để trống");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Lý do hủy không được vượt quá 500 ký tự");
        }
    }

    public static CancelReason of(String value) {
        return new CancelReason(value);
    }
}
