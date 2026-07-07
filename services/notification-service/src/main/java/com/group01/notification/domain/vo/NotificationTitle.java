package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class NotificationTitle {
    private static final int MAX_LENGTH = 255;
    private final String value;

    private NotificationTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = value.trim();
    }

    public static NotificationTitle of(String value) {
        return new NotificationTitle(value);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
