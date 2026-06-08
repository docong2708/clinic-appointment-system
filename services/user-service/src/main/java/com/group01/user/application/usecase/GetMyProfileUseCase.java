package com.group01.user.application.usecase;

import com.group01.commonsecurity.currentuser.CurrentUserHolder;
import com.group01.user.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMyProfileUseCase {
    private final GetUserByIdUseCase getUserByIdUseCase;

    @Transactional(readOnly = true)
    public User execute() {
        return getUserByIdUseCase.execute(CurrentUserHolder.require().userId());
    }
}