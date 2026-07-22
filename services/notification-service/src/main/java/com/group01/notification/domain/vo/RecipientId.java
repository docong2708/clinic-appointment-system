package com.group01.notification.domain.vo;

import lombok.EqualsAndHashCode;
import java.util.UUID;

@EqualsAndHashCode
public class RecipientId {
    private final UUID value;

    private RecipientId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Mã người nhận không được để trống");
        }
        this.value = value;
    }

    public static RecipientId of(UUID value) {
        return new RecipientId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
