package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV1;
import com.dmasone.identity.api.dto.UserResponseV1;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserServiceV1ImplTest {

    @Autowired
    private UserServiceV1 userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() {

        CreateUserRequestV1 request = new CreateUserRequestV1(
                "test@mail.com",
                "password123"
        );

        UserResponseV1 response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@mail.com");

        User saved = userRepository.findById(response.getId()).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("test@mail.com");
        assertThat(saved.getPasswordHash()).isNotBlank();
    }

    @Test
    void shouldNotAllowDuplicateEmail() {

        CreateUserRequestV1 request = new CreateUserRequestV1(
                "duplicate@mail.com",
                "password123"
        );

        userService.createUser(request);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void shouldGetUserById() {

        CreateUserRequestV1 request = new CreateUserRequestV1(
                "find@mail.com",
                "password123"
        );

        UserResponseV1 created = userService.createUser(request);

        UserResponseV1 found = userService.getUserById(created.getId().toString());

        assertThat(found.getEmail()).isEqualTo("find@mail.com");
    }

    @Test
    void shouldFailWhenUserNotFound() {

        assertThatThrownBy(() ->
                userService.getUserById("00000000-0000-0000-0000-000000000000")
        ).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldSoftDeleteUser() {

        CreateUserRequestV1 request = new CreateUserRequestV1(
                "delete@mail.com",
                "password123"
        );

        UserResponseV1 created = userService.createUser(request);

        userService.deleteUser(created.getId().toString());

        User deleted = userRepository.findById(created.getId()).orElseThrow();

        assertThat(deleted.getStatus().name()).isEqualTo("INACTIVE");
    }
}