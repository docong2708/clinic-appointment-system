package com.group01.doctor.domain.valueobject;

import java.util.UUID;

public record SlotId(UUID value) {

    public SlotId {
        if (value == null) {
            throw new IllegalArgumentException("Slot ID must not be null");
        }
    }

    public static SlotId of(UUID value) {
        return new SlotId(value);
    }

    public static SlotId generate() {
        return new SlotId(UUID.randomUUID());
    }
}
