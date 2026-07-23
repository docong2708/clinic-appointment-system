package com.group01.doctor.domain.valueobject;

import java.util.UUID;

public record SlotId(UUID value) {

    public SlotId {
        if (value == null) {
            throw new IllegalArgumentException("Mã khung giờ không được để trống");
        }
    }

    public static SlotId of(UUID value) {
        return new SlotId(value);
    }

    public static SlotId generate() {
        return new SlotId(UUID.randomUUID());
    }
}
