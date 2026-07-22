package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import java.util.UUID;

@EqualsAndHashCode
public class DeliveryId {
    private final UUID value;

    private DeliveryId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Mã lượt gửi thông báo không được để trống");
        }
        this.value = value;
    }

    public static DeliveryId of(UUID value) {
        return new DeliveryId(value);
    }

    public static DeliveryId generate() {
        return new DeliveryId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
