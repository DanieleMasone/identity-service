package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV2;
import com.dmasone.identity.api.dto.UpdateUserRequestV2;
import com.dmasone.identity.api.dto.UserResponseV2;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceV2ImplTest {

    @Autowired
    private UserServiceV2 userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() {

        CreateUserRequestV2 request = new CreateUserRequestV2(
                "test@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        UserResponseV2 response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());

        User saved = userRepository.findById(response.getId()).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo(request.getEmail());
        assertThat(saved.getPasswordHash()).startsWith("hashed_");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNotAllowDuplicateEmail() {

        CreateUserRequestV2 request = new CreateUserRequestV2(
                "duplicate@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        userService.createUser(request);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void shouldGetUserById() {

        CreateUserRequestV2 request = new CreateUserRequestV2(
                "find@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        UserResponseV2 created = userService.createUser(request);

        UserResponseV2 found = userService.getUserById(created.getId().toString());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void shouldFailWhenUserNotFound() {

        UUID fakeId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        assertThatThrownBy(() ->
                userService.getUserById(fakeId.toString())
        ).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldUpdateUserStatus() {

        CreateUserRequestV2 request = new CreateUserRequestV2(
                "update@mail.com",
                "password123",
                "Mario",
                "Rossi"
        );

        UserResponseV2 created = userService.createUser(request);

        UpdateUserRequestV2 updateRequest = new UpdateUserRequestV2();
        updateRequest.setStatus(UserStatus.INACTIVE);

        UserResponseV2 updated = userService.updateUser(
                created.getId().toString(),
                updateRequest
        );

        assertThat(updated).isNotNull();

        User saved = userRepository.findById(created.getId()).orElseThrow();

        assertThat(saved.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFailUpdateWhenUserNotFound() {

        UpdateUserRequestV2 request = new UpdateUserRequestV2();
        request.setStatus(UserStatus.ACTIVE);

        UUID fakeId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        assertThatThrownBy(() ->
                userService.updateUser(fakeId.toString(), request)
        ).isInstanceOf(UserNotFoundException.class);
    }
}