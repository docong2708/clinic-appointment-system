package com.group01.doctor.api.controller;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.doctor.application.dto.DoctorLeaveDto;
import com.group01.doctor.application.dto.RequestDoctorLeaveRequest;
import com.group01.doctor.application.usecase.CancelDoctorLeaveUseCase;
import com.group01.doctor.application.usecase.RequestDoctorLeaveUseCase;
import com.group01.doctor.application.usecase.ViewDoctorLeavesUseCase;
import com.group01.doctor.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/me/leaves")
@RequiredArgsConstructor
@RequireRole("DOCTOR")
@Deprecated(forRemoval = false, since = "2026-07")
public class DoctorLeaveController {

    private final RequestDoctorLeaveUseCase requestDoctorLeaveUseCase;
    private final ViewDoctorLeavesUseCase viewDoctorLeavesUseCase;
    private final CancelDoctorLeaveUseCase cancelDoctorLeaveUseCase;

    @PostMapping
    public ResponseEntity<DoctorLeaveDto> requestLeave(@Valid @RequestBody RequestDoctorLeaveRequest request) {
        UUID userId = CurrentUserHolder.require().userId();
        DoctorLeaveDto response = requestDoctorLeaveUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DoctorLeaveDto>> getMyLeaves() {
        UUID userId = CurrentUserHolder.require().userId();
        return ResponseEntity.ok(viewDoctorLeavesUseCase.execute(userId));
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> cancelLeave(@PathVariable("leaveId") UUID leaveId) {
        UUID userId = CurrentUserHolder.require().userId();
        cancelDoctorLeaveUseCase.execute(userId, leaveId);
        return ResponseEntity.noContent().build();
    }
}
