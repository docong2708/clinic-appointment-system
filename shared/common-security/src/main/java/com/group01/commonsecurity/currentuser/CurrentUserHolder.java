package com.group01.commonsecurity.currentuser;

import java.util.Optional;

public final class CurrentUserHolder {
    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static CurrentUser require() {
        return get().orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin người dùng hiện tại"));
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
