package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
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
 * Unit tests for version 1 user business behavior.
 *
 * <p>The tests isolate service logic from the database and verify the core
 * contract: creation, duplicate email protection, lookup failures, and soft
 * deletion.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceV1Test {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserServiceV1 userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        userService = new UserServiceV1(
                new UserServiceSupport(userRepository, passwordEncoder),
                Mappers.getMapper(UserMapper.class)
        );
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequestV1 request = new CreateUserRequestV1("test@mail.com", "password123");

        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        UserResponseV1 response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@mail.com");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        CreateUserRequestV1 request = new CreateUserRequestV1("duplicate@mail.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void shouldTranslateConcurrentUniqueEmailConflict() {
        CreateUserRequestV1 request = new CreateUserRequestV1("race@mail.com", "password123");
        DataIntegrityViolationException conflict = new DataIntegrityViolationException(
                "unique email",
                new SQLException("duplicate key", "23505")
        );

        when(userRepository.saveAndFlush(any(User.class))).thenThrow(conflict);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists")
                .hasCause(conflict);
    }

    @Test
    void shouldNotMaskUnrelatedPersistenceFailures() {
        CreateUserRequestV1 request = new CreateUserRequestV1("invalid@mail.com", "password123");
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "not-null violation",
                new SQLException("missing value", "23502")
        );

        when(userRepository.saveAndFlush(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> userService.createUser(request)).isSameAs(failure);
    }

    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("find@mail.com")
                .status(com.dmasone.identity.domain.model.UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponseV1 found = userService.getUserById(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getEmail()).isEqualTo("find@mail.com");
    }

    @Test
    void shouldFailWhenUserNotFound() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldSoftDeleteUser() {
        UUID id = UUID.randomUUID();
        Instant previousUpdate = Instant.parse("2026-01-01T00:00:00Z");
        User user = User.builder()
                .id(id)
                .email("delete@mail.com")
                .status(com.dmasone.identity.domain.model.UserStatus.ACTIVE)
                .createdAt(previousUpdate)
                .updatedAt(previousUpdate)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(id);

        assertThat(user.getStatus()).isEqualTo(com.dmasone.identity.domain.model.UserStatus.INACTIVE);
        assertThat(user.getUpdatedAt()).isAfter(previousUpdate);
        verify(userRepository).save(user);
    }

    @Test
    void shouldFailDeleteWhenUserNotFound() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}
