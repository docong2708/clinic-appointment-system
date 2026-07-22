package com.group01.doctor.application.usecase;

import com.group01.doctor.application.dto.AssignSlotRequest;
import com.group01.doctor.domain.exception.DomainException;
import com.group01.doctor.domain.model.AssignedSlot;
import com.group01.doctor.domain.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignSlotUseCaseTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Test
    void assignsAvailableDoctorSlot() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        AssignedSlot assignedSlot = assignedSlot(startTime);
        AssignSlotUseCase useCase = new AssignSlotUseCase(doctorRepository);

        when(doctorRepository.assignAvailableSlot("Tim mạch", startTime, startTime.plusMinutes(30)))
                .thenReturn(Optional.of(assignedSlot));

        var result = useCase.execute(new AssignSlotRequest("Tim mạch", startTime, startTime.plusMinutes(30)));

        assertThat(result.id()).isEqualTo(assignedSlot.id());
        assertThat(result.doctorId()).isEqualTo(assignedSlot.doctorId());
        assertThat(result.booked()).isTrue();
        assertThat(result.status()).isEqualTo("BOOKED");
    }

    @Test
    void throwsWhenNoDoctorSlotIsAvailable() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        AssignSlotUseCase useCase = new AssignSlotUseCase(doctorRepository);

        when(doctorRepository.assignAvailableSlot("Tim mạch", startTime, startTime.plusMinutes(30)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AssignSlotRequest("Tim mạch", startTime, startTime.plusMinutes(30))))
                .isInstanceOf(DomainException.class)
                .hasMessage("Không còn bác sĩ trống trong chuyên khoa và khung giờ đã chọn");
    }

    private AssignedSlot assignedSlot(LocalDateTime startTime) {
        return new AssignedSlot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Bác sĩ A",
                "Tim mạch",
                "0900000000",
                "doctor@example.com",
                startTime,
                startTime.plusMinutes(30),
                true,
                "BOOKED"
        );
    }
}
