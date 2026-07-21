package com.group01.user.application.usecase;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.UserNotFoundException;
import com.group01.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetMyProfileUseCase {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute() {
        var userId = CurrentUserHolder.require().userId();
        log.info("Get my profile requested userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found: " + userId));
        log.info("Get my profile completed userId={} email={}", user.getId(), user.getEmail().value());
        return user;
    }
}
