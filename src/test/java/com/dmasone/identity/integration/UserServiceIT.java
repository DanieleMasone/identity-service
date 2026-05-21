package com.dmasone.identity.integration;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.api.generated.model.UserStatus;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import com.dmasone.identity.service.UserServiceV1;
import com.dmasone.identity.service.UserServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests that verify service behavior against a real PostgreSQL container.
 *
 * <p>The suite exercises Flyway migrations, Spring wiring, JPA persistence,
 * password hashing, duplicate checks, partial updates, and soft deletes.</p>
 */
class UserServiceIT extends PostgresIntegrationTest {

    @Autowired
    UserServiceV1 userService;

    @Autowired
    UserServiceV2 userServiceV2;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateReadAndPersistV1User() {
        CreateUserRequestV1 request = new CreateUserRequestV1("test@test.com", "password123");

        UserResponseV1 created = userService.createUser(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");
        assertThat(created.getStatus()).isEqualTo(UserStatus.ACTIVE);

        UserResponseV1 found = userService.getUserById(created.getId());

        assertThat(found.getEmail()).isEqualTo("test@test.com");
        assertThat(userRepository.findById(created.getId()))
                .get()
                .satisfies(user -> {
                    assertThat(user.getPasswordHash()).isNotEqualTo("password123");
                    assertThat(user.getStatus()).isEqualTo(com.dmasone.identity.domain.model.UserStatus.ACTIVE);
                });
    }

    @Test
    void shouldCreateUpdateAndReadV2User() {
        UserResponseV2 created = userServiceV2.createUser(new CreateUserRequestV2(
                "profile@test.com",
                "password123",
                "Mario",
                "Rossi"
        ));

        UserResponseV2 updated = userServiceV2.updateUser(created.getId(), new UpdateUserRequestV2()
                .firstName("Luigi")
                .lastName("Verdi")
                .status(UserStatus.SUSPENDED));

        assertThat(updated.getFirstName()).isEqualTo("Luigi");
        assertThat(updated.getLastName()).isEqualTo("Verdi");
        assertThat(updated.getStatus()).isEqualTo(UserStatus.SUSPENDED);

        UserResponseV2 found = userServiceV2.getUserById(created.getId());

        assertThat(found.getEmail()).isEqualTo("profile@test.com");
        assertThat(found.getFirstName()).isEqualTo("Luigi");
        assertThat(found.getLastName()).isEqualTo("Verdi");
        assertThat(found.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void shouldSoftDeletePersistedUser() {
        UserResponseV1 created = userService.createUser(new CreateUserRequestV1("delete@test.com", "password123"));

        userService.deleteUser(created.getId());

        assertThat(userRepository.findById(created.getId()))
                .get()
                .extracting(User::getStatus)
                .isEqualTo(com.dmasone.identity.domain.model.UserStatus.INACTIVE);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userService.createUser(new CreateUserRequestV1("duplicate@test.com", "password123"));

        assertThatThrownBy(() -> userServiceV2.createUser(new CreateUserRequestV2(
                "duplicate@test.com",
                "password456",
                "Maria",
                "Bianchi"
        )))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        assertThat(userRepository.findByEmail("duplicate@test.com")).isPresent();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        assertThatThrownBy(() -> userService.getUserById(missingId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userServiceV2.updateUser(missingId, new UpdateUserRequestV2().status(UserStatus.ACTIVE)))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userService.deleteUser(missingId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
