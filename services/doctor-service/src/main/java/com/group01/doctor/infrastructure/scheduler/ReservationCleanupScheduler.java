package com.group01.doctor.infrastructure.scheduler;

import com.group01.doctor.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupScheduler {

    private final DoctorRepository doctorRepository;

    /**
     * Runs every 60 seconds (60,000 milliseconds) to clean up expired reservations.
     * Releases slots that were reserved more than 10 minutes ago.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        log.debug("Starting reservation cleanup. Cutoff time: {}", cutoffTime);

        int updatedRows = doctorRepository.releaseExpiredSlots(cutoffTime);

        if (updatedRows > 0) {
            log.info("Released {} expired reserved doctor slots.", updatedRows);
        }
    }
}
