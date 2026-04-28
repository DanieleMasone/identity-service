package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.api.generated.model.UserStatus;
import com.dmasone.identity.api.mapper.UserMapper;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for version 2 user business behavior.
 *
 * <p>The suite documents the API evolution introduced in v2: profile fields,
 * partial updates, duplicate email protection, and password hashing.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceV2ImplTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserServiceV2 userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        userService = new UserServiceV2Impl(
                userRepository,
                Mappers.getMapper(UserMapper.class),
                passwordEncoder
        );
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequestV2 request = new CreateUserRequestV2(
                "test@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        UserResponseV2 response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getFirstName()).isEqualTo("Mario");
        assertThat(response.getLastName()).isEqualTo("Rossi");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getFirstName()).isEqualTo("Mario");
        assertThat(saved.getLastName()).isEqualTo("Rossi");
        assertThat(saved.getStatus()).isEqualTo(com.dmasone.identity.domain.model.UserStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        CreateUserRequestV2 request = new CreateUserRequestV2(
                "duplicate@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseV2 found = userService.getUserById(id);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getEmail()).isEqualTo("find@mail.com");
        assertThat(found.getFirstName()).isEqualTo("Mario");
        assertThat(found.getLastName()).isEqualTo("Rossi");
    }

    @Test
    void shouldFailWhenUserNotFound() {
        UUID fakeId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(userRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(fakeId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldUpdateUserProfileAndStatus() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id);
        UpdateUserRequestV2 updateRequest = new UpdateUserRequestV2()
                .firstName("Luigi")
                .lastName("Verdi")
                .status(UserStatus.INACTIVE);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseV2 updated = userService.updateUser(id, updateRequest);

        assertThat(updated).isNotNull();
        assertThat(updated.getFirstName()).isEqualTo("Luigi");
        assertThat(updated.getLastName()).isEqualTo("Verdi");
        assertThat(updated.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getStatus()).isEqualTo(com.dmasone.identity.domain.model.UserStatus.INACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id);
        UpdateUserRequestV2 updateRequest = new UpdateUserRequestV2()
                .firstName("Luigi");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseV2 updated = userService.updateUser(id, updateRequest);

        assertThat(updated.getFirstName()).isEqualTo("Luigi");
        assertThat(updated.getLastName()).isEqualTo("Rossi");
        assertThat(updated.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getFirstName()).isEqualTo("Luigi");
        assertThat(user.getLastName()).isEqualTo("Rossi");
        assertThat(user.getStatus()).isEqualTo(com.dmasone.identity.domain.model.UserStatus.ACTIVE);
    }

    @Test
    void shouldFailUpdateWhenUserNotFound() {
        UUID fakeId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        UpdateUserRequestV2 request = new UpdateUserRequestV2().status(UserStatus.ACTIVE);

        when(userRepository.findById(fakeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(fakeId, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    private User buildUser(UUID id) {
        return User.builder()
                .id(id)
                .email("find@mail.com")
                .passwordHash("hash")
                .firstName("Mario")
                .lastName("Rossi")
                .status(com.dmasone.identity.domain.model.UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
