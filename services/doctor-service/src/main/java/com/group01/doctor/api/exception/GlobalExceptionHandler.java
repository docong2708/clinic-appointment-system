package com.group01.doctor.api.exception;

import com.group01.doctor.domain.exception.DoctorNotFoundException;
import com.group01.doctor.domain.exception.DoctorLeaveConflictException;
import com.group01.doctor.domain.exception.BadRequestException;
import com.group01.doctor.domain.exception.DomainException;
import com.group01.doctor.domain.exception.SlotOverlapException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
    public void handleClientAbort(AsyncRequestNotUsableException ex, HttpServletRequest request) {
        log.debug("Client disconnected before response completed path={} reason={}",
                request.getRequestURI(), rootCauseMessage(ex));
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(DoctorNotFoundException ex, HttpServletRequest request) {
        log.warn("Doctor not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(SlotOverlapException.class)
    public ResponseEntity<ErrorResponse> handleSlotOverlap(SlotOverlapException ex, HttpServletRequest request) {
        log.warn("Slot overlap: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DoctorLeaveConflictException.class)
    public ResponseEntity<ErrorResponse> handleDoctorLeaveConflict(DoctorLeaveConflictException ex, HttpServletRequest request) {
        log.warn("Doctor leave conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleDomainException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Domain exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "Invalid value for '" + ex.getName() + "': " + ex.getValue();
        log.warn("Path/query parameter type mismatch: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String rootCause = rootCauseMessage(ex);
        log.warn("Request body is invalid: {}", rootCause);
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body is invalid: " + rootCause, request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String rootCause = rootCauseMessage(ex);
        log.error("Database integrity violation: {}", rootCause, ex);
        return buildResponse(HttpStatus.CONFLICT, "Database constraint violation: " + rootCause, request, null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request
    ) {
        String rootCause = rootCauseMessage(ex);
        log.error("Optimistic locking failure: {}", rootCause, ex);
        return buildResponse(HttpStatus.CONFLICT, "Data was modified by another request. Please reload and try again.", request, null);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException ex,
            HttpServletRequest request
    ) {
        log.warn("Response status exception: {}", ex.getMessage());
        int statusCode = ex.getStatusCode().value();
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                statusCode,
                reasonPhrase(statusCode),
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception rootCause={}", rootCauseMessage(ex), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> details
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(error);
    }

    private String reasonPhrase(int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status == null ? "HTTP " + statusCode : status.getReasonPhrase();
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(ex);
        return rootCause == null || rootCause.getMessage() == null || rootCause.getMessage().isBlank()
                ? ex.getClass().getSimpleName()
                : rootCause.getMessage();
    }
}
