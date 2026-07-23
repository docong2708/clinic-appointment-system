package com.group01.payment.api.controller;

import com.group01.payment.api.dto.ErrorResponse;
import com.group01.payment.application.exception.AppointmentNotFoundException;
import com.group01.payment.application.exception.AppointmentServiceUnavailableException;
import com.group01.payment.domain.exception.DuplicatePaymentException;
import com.group01.payment.domain.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler({
            PaymentNotFoundException.class,
            AppointmentNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        String message = exception instanceof HttpMessageNotReadableException
                ? "Dữ liệu request không hợp lệ"
                : rootCauseMessage(exception);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler({
            IllegalStateException.class,
            DuplicatePaymentException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(AppointmentServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(
            AppointmentServiceUnavailableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, null);
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

        return buildResponse(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception path={} rootCause={}", request.getRequestURI(), rootCauseMessage(exception), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định", request, null);
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
                reasonPhrase(status),
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

    private String reasonPhrase(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Yêu cầu không hợp lệ";
            case NOT_FOUND -> "Không tìm thấy";
            case CONFLICT -> "Xung đột dữ liệu";
            case SERVICE_UNAVAILABLE -> "Dịch vụ tạm thời không khả dụng";
            case INTERNAL_SERVER_ERROR -> "Lỗi hệ thống";
            default -> status.getReasonPhrase();
        };
    }
}
