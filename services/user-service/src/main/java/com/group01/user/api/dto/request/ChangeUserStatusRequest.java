package com.group01.user.api.dto.request;

import com.group01.user.domain.vo.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(@NotNull UserStatus status) {
}
