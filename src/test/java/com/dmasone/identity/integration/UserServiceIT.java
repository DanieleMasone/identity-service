package com.dmasone.identity.integration;

import com.dmasone.identity.api.dto.CreateUserRequestV1;
import com.dmasone.identity.api.dto.UserResponseV1;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.service.UserServiceV1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class UserServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("identity_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserServiceV1 userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldCreateUser_readUser_andPersistInDb() {
        // CREATE
        CreateUserRequestV1 request = new CreateUserRequestV1();
        request.setEmail("test@test.com");
        request.setPassword("Dani");

        UserResponseV1 created = userService.createUser(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");

        // READ
        UserResponseV1 found = userService.getUserById(created.getId().toString());

        assertThat(found.getEmail()).isEqualTo("test@test.com");

        // VERIFY DB STATE
        assertThat(userRepository.findById(created.getId())).isPresent();
    }
}
