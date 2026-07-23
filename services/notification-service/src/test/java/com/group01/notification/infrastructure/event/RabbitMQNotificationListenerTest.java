package com.group01.notification.infrastructure.event;

import com.group01.commonevents.appointment.AppointmentCanceledEvent;
import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.appointment.AppointmentUpdatedEvent;
import com.group01.notification.application.command.CreateNotificationCommand;
import com.group01.notification.application.usecase.CreateNotificationUseCase;
import com.group01.notification.domain.aggregate.NotificationInboxEvent;
import com.group01.notification.domain.repository.NotificationInboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitMQNotificationListenerTest {

    @Mock
    private NotificationInboxEventRepository inboxEventRepository;

    @Mock
    private CreateNotificationUseCase createNotificationUseCase;

    private RabbitMQNotificationListener listener;

    @BeforeEach
    public void setUp() {
        listener = new RabbitMQNotificationListener(inboxEventRepository, createNotificationUseCase);
    }

    @Test
    public void testHandleAppointmentCreated() {
        UUID eventId = UUID.randomUUID();
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                eventId,
                appointmentId,
                patientUserId,
                patientId,
                "patient@example.com",
                doctorId,
                "Dr. Tran Thi B",
                "Nhi khoa",
                slotId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Regular checkup",
                "CONFIRMED",
                LocalDateTime.now()
        );

        when(inboxEventRepository.existsBySourceEventId(eventId)).thenReturn(false);
        doAnswer(inv -> inv.getArgument(0)).when(inboxEventRepository).save(any(NotificationInboxEvent.class));

        listener.handleAppointmentCreated(event);

        verify(inboxEventRepository, times(2)).save(any(NotificationInboxEvent.class));
        verify(inboxEventRepository).existsBySourceEventId(eventId);
        ArgumentCaptor<CreateNotificationCommand> commandCaptor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(createNotificationUseCase).handle(commandCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(commandCaptor.getValue().getDestination()).isEqualTo("patient@example.com");
        org.assertj.core.api.Assertions.assertThat(commandCaptor.getValue().getRecipientUserId()).isEqualTo(patientUserId);
    }

    @Test
    public void testDeduplicationSkip() {
        UUID eventId = UUID.randomUUID();
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                eventId,
                appointmentId,
                patientUserId,
                patientId,
                "patient@example.com",
                doctorId,
                "Dr. Tran Thi B",
                "Nhi khoa",
                slotId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "Regular checkup",
                "CONFIRMED",
                LocalDateTime.now()
        );

        when(inboxEventRepository.existsBySourceEventId(eventId)).thenReturn(true);

        listener.handleAppointmentCreated(event);

        verify(inboxEventRepository).existsBySourceEventId(eventId);
        verify(inboxEventRepository, never()).save(any(NotificationInboxEvent.class));
        verify(createNotificationUseCase, never()).handle(any(CreateNotificationCommand.class));
    }

    @Test
    public void testHandleAppointmentCanceledUsesPatientEmailAndReadableBody() {
        UUID eventId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        AppointmentCanceledEvent event = new AppointmentCanceledEvent(
                eventId,
                appointmentId,
                patientUserId,
                patientId,
                "patient@example.com",
                doctorId,
                "Dr. Tran Thi B",
                "Nhi khoa",
                startTime,
                startTime.plusMinutes(30),
                "Cannot attend",
                UUID.randomUUID(),
                "PATIENT",
                LocalDateTime.now(),
                "CANCELLED",
                LocalDateTime.now()
        );

        when(inboxEventRepository.existsBySourceEventId(eventId)).thenReturn(false);
        doAnswer(inv -> inv.getArgument(0)).when(inboxEventRepository).save(any(NotificationInboxEvent.class));

        listener.handleAppointmentCanceled(event);

        ArgumentCaptor<CreateNotificationCommand> commandCaptor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(createNotificationUseCase).handle(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(command.getDestination()).isEqualTo("patient@example.com");
        org.assertj.core.api.Assertions.assertThat(command.getRecipientUserId()).isEqualTo(patientUserId);
        org.assertj.core.api.Assertions.assertThat(command.getBody()).contains("Dr. Tran Thi B", "Nhi khoa", "Cannot attend");
        org.assertj.core.api.Assertions.assertThat(command.getBody()).doesNotContain(appointmentId.toString(), doctorId.toString());
    }

    @Test
    public void testHandleAppointmentUpdatedUsesPatientEmailAndOldNewTimeDetails() {
        UUID eventId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID oldSlotId = UUID.randomUUID();
        UUID newSlotId = UUID.randomUUID();
        LocalDateTime previousStartTime = LocalDateTime.now().plusDays(1);
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2);

        AppointmentUpdatedEvent event = new AppointmentUpdatedEvent(
                eventId,
                appointmentId,
                patientUserId,
                patientId,
                "patient@example.com",
                doctorId,
                "Dr. Tran Thi B",
                "Nhi khoa",
                oldSlotId,
                previousStartTime,
                previousStartTime.plusMinutes(30),
                newSlotId,
                null,
                newStartTime,
                newStartTime.plusMinutes(30),
                "Change appointment time",
                "CONFIRMED",
                "WEB",
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(inboxEventRepository.existsBySourceEventId(eventId)).thenReturn(false);
        doAnswer(inv -> inv.getArgument(0)).when(inboxEventRepository).save(any(NotificationInboxEvent.class));

        listener.handleAppointmentUpdated(event);

        ArgumentCaptor<CreateNotificationCommand> commandCaptor = ArgumentCaptor.forClass(CreateNotificationCommand.class);
        verify(createNotificationUseCase).handle(commandCaptor.capture());
        CreateNotificationCommand command = commandCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(command.getDestination()).isEqualTo("patient@example.com");
        org.assertj.core.api.Assertions.assertThat(command.getRecipientUserId()).isEqualTo(patientUserId);
        org.assertj.core.api.Assertions.assertThat(command.getBody()).contains(
                "Dr. Tran Thi B",
                "Thời gian cũ",
                previousStartTime.toString(),
                "Thời gian mới",
                newStartTime.toString()
        );
        org.assertj.core.api.Assertions.assertThat(command.getBody()).doesNotContain(appointmentId.toString(), doctorId.toString());
    }
}
