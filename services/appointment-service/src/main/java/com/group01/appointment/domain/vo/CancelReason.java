package com.group01.appointment.domain.vo;

public record CancelReason(String value) {

    private static final int MAX_LENGTH = 500;

    public CancelReason {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Cancel reason must not be blank");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Cancel reason must not exceed 500 characters");
        }
    }

    public static CancelReason of(String value) {
        return new CancelReason(value);
    }
}
