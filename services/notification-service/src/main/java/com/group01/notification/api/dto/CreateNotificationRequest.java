package com.group01.notification.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    @NotNull(message = "Mã người nhận không được để trống")
    private java.util.UUID recipientUserId;

    @NotBlank(message = "Loại thông báo không được để trống")
    private String type;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String body;

    @NotNull(message = "Mức ưu tiên không được để trống")
    private Short priority;

    @NotBlank(message = "Kênh gửi không được để trống")
    private String channel;

    @NotBlank(message = "Địa chỉ nhận không được để trống")
    private String destination;
}
