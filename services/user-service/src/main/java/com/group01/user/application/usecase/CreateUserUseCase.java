package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.domain.aggregate.Role;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.exception.EmailAlreadyExistsException;
import com.group01.user.domain.exception.PhoneAlreadyExistsException;
import com.group01.user.domain.exception.RoleNotFoundException;
import com.group01.user.domain.repository.RoleRepository;
import com.group01.user.domain.repository.UserRepository;
import com.group01.user.domain.vo.Email;
import com.group01.user.domain.vo.PhoneNumber;
import com.group01.user.domain.vo.RoleName;
import com.group01.user.domain.vo.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User execute(CreateUserCommand command) {
        Email email = new Email(command.email());
        PhoneNumber phoneNumber = new PhoneNumber(command.phoneNumber());
        if (userRepository.existsByEmail(email.value())) {
            throw new EmailAlreadyExistsException("Email already exists: " + email.value());
        }
        if (phoneNumber.value() != null && userRepository.existsByPhoneNumber(phoneNumber.value())) {
            throw new PhoneAlreadyExistsException("Phone number already exists: " + phoneNumber.value());
        }
        Set<Role> roles = resolveRoles(command.roles());
        User user = User.builder()
                .email(email)
                .fullName(requireFullName(command.fullName()))
                .phoneNumber(phoneNumber)
                .passwordHash(passwordEncoder.encode(command.password()))
                .status(UserStatus.PENDING_VERIFY)
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<String> roleNames = requestedRoles == null || requestedRoles.isEmpty()
                ? Set.of(RoleName.PATIENT.name())
                : requestedRoles.stream().map(role -> RoleName.from(role).name()).collect(Collectors.toSet());
        Set<Role> roles = new HashSet<>(roleRepository.findByNames(roleNames));
        if (roles.size() != roleNames.size()) {
            throw new RoleNotFoundException("One or more roles do not exist");
        }
        return roles;
    }

    private String requireFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        return fullName.trim();
    }
}
