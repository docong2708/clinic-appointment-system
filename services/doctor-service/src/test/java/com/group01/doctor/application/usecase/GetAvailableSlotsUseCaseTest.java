package com.group01.doctor.application.usecase;

import com.group01.doctor.domain.model.AvailableSlot;
import com.group01.doctor.domain.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableSlotsUseCaseTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Test
    void returnsAvailableSlotsGroupedByTime() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime startTime = date.atTime(9, 0);
        GetAvailableSlotsUseCase useCase = new GetAvailableSlotsUseCase(doctorRepository);

        when(doctorRepository.findAvailableSlotsBySpecialization(
                org.mockito.ArgumentMatchers.eq("Tim mạch"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(new AvailableSlot(startTime, startTime.plusMinutes(30), 2L)));

        var result = useCase.execute("Tim mạch", date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).startTime()).isEqualTo(startTime);
        assertThat(result.get(0).endTime()).isEqualTo(startTime.plusMinutes(30));
        assertThat(result.get(0).availableCount()).isEqualTo(2L);
    }
}
