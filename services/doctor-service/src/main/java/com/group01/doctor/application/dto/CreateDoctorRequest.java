package com.group01.doctor.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorRequest {
    @NotNull(message = "Mã người dùng không được để trống")
    private UUID userId;

    @NotBlank(message = "Tên bác sĩ không được để trống")
    private String name;

    @NotBlank(message = "Chuyên khoa không được để trống")
    private String specialization;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;
}
