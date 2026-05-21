package com.dmasone.identity.controller;

import com.dmasone.identity.api.controller.UsersV2Controller;
import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.api.generated.model.UserStatus;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.GlobalExceptionHandler;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import com.dmasone.identity.service.UserServiceV2;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the version 2 users controller.
 */
@ExtendWith(MockitoExtension.class)
class UsersV2ControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private UserServiceV2 userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = buildDefaultValidatorFactory();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsersV2Controller(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new SpringValidatorAdapter(validatorFactory.getValidator()))
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseV2 response = userResponse(id)
                .email("created@mail.com")
                .firstName("Mario")
                .lastName("Rossi");

        when(userService.createUser(any(CreateUserRequestV2.class))).thenReturn(response);

        mockMvc.perform(post("/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "created@mail.com",
                                "password", "password123",
                                "firstName", "Mario",
                                "lastName", "Rossi"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("created@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CreateUserRequestV2> requestCaptor = ArgumentCaptor.forClass(CreateUserRequestV2.class);
        verify(userService).createUser(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getEmail()).isEqualTo("created@mail.com");
        assertThat(requestCaptor.getValue().getFirstName()).isEqualTo("Mario");
        assertThat(requestCaptor.getValue().getLastName()).isEqualTo("Rossi");
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(CreateUserRequestV2.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "duplicate@mail.com",
                                "password", "password123",
                                "firstName", "Mario",
                                "lastName", "Rossi"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Email already exists"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Email already exists"));
    }

    @Test
    void shouldRejectIncompleteCreatePayload() throws Exception {
        mockMvc.perform(post("/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "created@mail.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists());

        verify(userService, never()).createUser(any(CreateUserRequestV2.class));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseV2 response = userResponse(id)
                .firstName("Luigi")
                .lastName("Verdi")
                .status(UserStatus.SUSPENDED);

        when(userService.updateUser(any(UUID.class), any(UpdateUserRequestV2.class))).thenReturn(response);

        mockMvc.perform(patch("/v2/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Luigi",
                                "lastName", "Verdi",
                                "status", "SUSPENDED"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Luigi"))
                .andExpect(jsonPath("$.lastName").value("Verdi"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        ArgumentCaptor<UpdateUserRequestV2> requestCaptor = ArgumentCaptor.forClass(UpdateUserRequestV2.class);
        verify(userService).updateUser(eq(id), requestCaptor.capture());

        assertThat(requestCaptor.getValue().getFirstName()).isEqualTo("Luigi");
        assertThat(requestCaptor.getValue().getLastName()).isEqualTo("Verdi");
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void shouldRejectInvalidUpdatePayload() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/v2/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", ""
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists());

        verify(userService, never()).updateUser(any(UUID.class), any(UpdateUserRequestV2.class));
    }

    @Test
    void shouldReturnProblemDetailWhenUpdateTargetDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(any(UUID.class), any(UpdateUserRequestV2.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/v2/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "INACTIVE"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("User not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void shouldRejectMalformedUserId() throws Exception {
        mockMvc.perform(get("/v2/users/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid path parameter"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Parameter 'id' has an invalid value"));

        verify(userService, never()).getUserById(any(UUID.class));
    }

    private UserResponseV2 userResponse(UUID id) {
        return new UserResponseV2()
                .id(id)
                .email("user@mail.com")
                .status(UserStatus.ACTIVE)
                .createdAt(OffsetDateTime.parse("2026-04-28T08:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-04-28T08:05:00Z"));
    }
}
