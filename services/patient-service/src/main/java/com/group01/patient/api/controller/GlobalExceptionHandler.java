package com.group01.patient.api.controller;

import com.group01.patient.api.dto.ErrorResponse;
import com.group01.patient.domain.exception.DomainException;
import com.group01.patient.domain.exception.MedicalRecordNotFoundException;
import com.group01.patient.domain.exception.PatientNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientAbort(AsyncRequestNotUsableException exception, HttpServletRequest request) {
        log.debug("Client disconnected before response completed path={} reason={}",
                request.getRequestURI(), rootCauseMessage(exception));
    }

    @ExceptionHandler({PatientNotFoundException.class, MedicalRecordNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception path={} rootCause={}", request.getRequestURI(), rootCauseMessage(exception), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> details
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(response);
    }

    private String rootCauseMessage(Throwable exception) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(exception);
        return rootCause == null || rootCause.getMessage() == null || rootCause.getMessage().isBlank()
                ? exception.getClass().getSimpleName()
                : rootCause.getMessage();
    }
}
