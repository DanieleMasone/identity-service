package com.dmasone.identity.integration;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.service.UserServiceV1;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies service behavior against a real PostgreSQL container.
 *
 * <p>The test exercises Flyway migrations, Spring wiring, JPA persistence, and
 * the service contract in a production-like database environment.</p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class UserServiceIT {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16");

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName("identity_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

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
        request.setPassword("password123");

        UserResponseV1 created = userService.createUser(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.com");

        // READ
        UserResponseV1 found = userService.getUserById(created.getId());

        assertThat(found.getEmail()).isEqualTo("test@test.com");

        // VERIFY DB STATE
        assertThat(userRepository.findById(created.getId())).isPresent();
    }
}
