package com.dmasone.identity.controller;

import com.dmasone.identity.api.controller.UsersV1Controller;
import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.api.generated.model.UserStatus;
import com.dmasone.identity.infrastructure.exception.GlobalExceptionHandler;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import com.dmasone.identity.service.UserServiceV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the version 1 users controller.
 */
@ExtendWith(MockitoExtension.class)
class UsersV1ControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private UserServiceV1 userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = buildDefaultValidatorFactory();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsersV1Controller(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(validatorFactory.getValidator()))
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseV1 response = new UserResponseV1()
                .id(id)
                .email("created@mail.com")
                .status(UserStatus.ACTIVE)
                .createdAt(OffsetDateTime.parse("2026-04-28T08:00:00Z"));

        when(userService.createUser(any(CreateUserRequestV1.class))).thenReturn(response);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "created@mail.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("created@mail.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CreateUserRequestV1> requestCaptor = ArgumentCaptor.forClass(CreateUserRequestV1.class);
        verify(userService).createUser(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getEmail()).isEqualTo("created@mail.com");
        assertThat(requestCaptor.getValue().getPassword()).isEqualTo("password123");
    }

    @Test
    void shouldRejectInvalidCreatePayload() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "not-an-email",
                                "password", "short"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void shouldReturnProblemDetailWhenUserDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(id);
    }
}
