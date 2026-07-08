package com.group01.notification.api.controller;

import com.group01.notification.api.dto.CreateNotificationRequest;
import com.group01.notification.api.dto.NotificationResponse;
import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.application.usecase.*;
import com.group01.notification.domain.aggregate.NotificationAggregate;
import com.group01.notification.domain.vo.NotificationId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final CreateNotificationUseCase createUseCase;
    private final GetNotificationUseCase getUseCase;
    private final ListNotificationsUseCase listUseCase;
    private final UpdateNotificationUseCase updateUseCase;
    private final DeleteNotificationUseCase deleteUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        CreateNotificationCommand command = CreateNotificationCommand.builder()
                .recipientUserId(request.getRecipientUserId())
                .type(request.getType())
                .title(request.getTitle())
                .body(request.getBody())
                .priority(request.getPriority())
                .channel(request.getChannel())
                .destination(request.getDestination())
                .sourceService("notification-service")
                .sourceEventId(UUID.randomUUID())
                .dedupeKey(UUID.randomUUID().toString())
                .aggregateType("Notification")
                .aggregateId(request.getRecipientUserId())
                .build();
        
        NotificationAggregate result = createUseCase.handle(command);
        return toResponse(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable("id") UUID id) {
        NotificationId notificationId = NotificationId.of(id);
        NotificationAggregate result = getUseCase.handle(notificationId);
        return ResponseEntity.ok(toResponse(result));
    }

    @GetMapping("/recipient/{recipientId}")
    public List<NotificationResponse> getByRecipient(@PathVariable("recipientId") UUID recipientId) {
        List<NotificationAggregate> results = listUseCase.handleByRecipientId(recipientId);
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/recipient/{recipientId}/unread")
    public List<NotificationResponse> getUnreadByRecipient(@PathVariable("recipientId") UUID recipientId) {
        List<NotificationAggregate> results = listUseCase.handleUnreadByRecipientId(recipientId);
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable("id") UUID id) {
        NotificationAggregate result = getUseCase.handle(NotificationId.of(id));
        result.markAsRead();
        return toResponse(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        NotificationId notificationId = NotificationId.of(id);
        deleteUseCase.handle(notificationId);
    }

    private NotificationResponse toResponse(NotificationAggregate aggregate) {
        return NotificationResponse.builder()
                .id(aggregate.getId().value())
                .recipientUserId(aggregate.getRecipientId().value())
                .type(aggregate.getType())
                .title(aggregate.getTitle().value())
                .body(aggregate.getBody())
                .status(aggregate.getStatus().name())
                .priority(aggregate.getPriority())
                .createdAt(aggregate.getCreatedAt())
                .updatedAt(aggregate.getUpdatedAt())
                .build();
    }
}
