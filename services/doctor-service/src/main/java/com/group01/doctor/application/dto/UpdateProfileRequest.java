package com.group01.doctor.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Tên bác sĩ không được để trống")
    private String name;

    @NotBlank(message = "Chuyên khoa không được để trống")
    private String specialization;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String biography;
    private String qualifications;
    private String avatarUrl;
}
