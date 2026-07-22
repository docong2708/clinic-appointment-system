package com.group01.notification.infrastructure.event;

import com.group01.commonevents.appointment.AppointmentCreatedEvent;
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
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                eventId,
                appointmentId,
                patientId,
                "patient@example.com",
                doctorId,
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
    }

    @Test
    public void testDeduplicationSkip() {
        UUID eventId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                eventId,
                appointmentId,
                patientId,
                "patient@example.com",
                doctorId,
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
}
