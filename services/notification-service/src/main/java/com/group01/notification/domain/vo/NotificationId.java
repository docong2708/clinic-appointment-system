package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import java.util.UUID;

@EqualsAndHashCode
public class NotificationId {
    private final UUID value;

    private NotificationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationId cannot be null");
        }
        this.value = value;
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
