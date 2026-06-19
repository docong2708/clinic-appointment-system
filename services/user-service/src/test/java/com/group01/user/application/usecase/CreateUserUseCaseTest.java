package com.group01.user.application.usecase;

import com.group01.user.application.command.CreateUserCommand;
import com.group01.user.domain.aggregate.Role;
import com.group01.user.domain.aggregate.User;
import com.group01.user.domain.repository.RoleRepository;
import com.group01.user.domain.repository.UserRepository;
import com.group01.user.domain.vo.RoleName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @Test
    void createsUserProfileMappedToKeycloakWithDefaultPatientRole() {
        CreateUserUseCase useCase = new CreateUserUseCase(userRepository, roleRepository);
        Role patient = Role.builder().id(UUID.randomUUID()).name(RoleName.PATIENT).description("Patient user").build();
        when(userRepository.existsByKeycloakUserId("kc-user-id")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByNames(Set.of("PATIENT"))).thenReturn(List.of(patient));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = useCase.execute(new CreateUserCommand("kc-user-id", "John@Example.com", "John Doe", "0123456789", null));

        assertThat(created.getKeycloakUserId()).isEqualTo("kc-user-id");
        assertThat(created.getEmail().value()).isEqualTo("john@example.com");
        assertThat(created.getRoles()).extracting(role -> role.getName().name()).containsExactly("PATIENT");
        assertThat(created.getStatus().name()).isEqualTo("ACTIVE");
    }
}
