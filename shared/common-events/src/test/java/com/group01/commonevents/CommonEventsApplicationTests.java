package com.group01.commonevents;

import com.group01.commonevents.appointment.AppointmentCreatedEvent;
import com.group01.commonevents.messaging.RabbitMQConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommonEventsApplicationTests {

    @Test
    void appointmentCreatedEventCarriesPayload() {
        UUID eventId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientUserId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 23, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 23, 9, 30);
        LocalDateTime occurredAt = LocalDateTime.of(2026, 7, 23, 8, 45);

        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                eventId,
                appointmentId,
                patientUserId,
                patientId,
                "patient@example.com",
                doctorId,
                "Doctor One",
                "Cardiology",
                slotId,
                startTime,
                endTime,
                "Checkup",
                "PENDING",
                occurredAt
        );

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.appointmentId()).isEqualTo(appointmentId);
        assertThat(event.patientEmail()).isEqualTo("patient@example.com");
        assertThat(event.doctorId()).isEqualTo(doctorId);
        assertThat(event.slotId()).isEqualTo(slotId);
        assertThat(event.status()).isEqualTo("PENDING");
    }

    @Test
    void appointmentRoutingPatternMatchesAppointmentEvents() {
        assertThat(RabbitMQConstants.APPOINTMENT_ROUTING_PATTERN).isEqualTo("appointment.*");
    }
}
