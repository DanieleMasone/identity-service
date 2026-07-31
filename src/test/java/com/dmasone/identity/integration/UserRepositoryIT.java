package com.dmasone.identity.integration;

import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository integration tests backed by real PostgreSQL.
 */
class UserRepositoryIT extends PostgresIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRoundTripDomainFieldsAndTimestamps() {
        User saved = userRepository.saveAndFlush(user("repository@test.com", UserStatus.SUSPENDED));
        entityManager.clear();

        assertThat(userRepository.findById(saved.getId()))
                .get()
                .satisfies(reloaded -> {
                    assertThat(reloaded.getEmail()).isEqualTo("repository@test.com");
                    assertThat(reloaded.getFirstName()).isEqualTo("Test");
                    assertThat(reloaded.getLastName()).isEqualTo("User");
                    assertThat(reloaded.getStatus()).isEqualTo(UserStatus.SUSPENDED);
                    assertThat(reloaded.getCreatedAt()).isEqualTo(Instant.parse("2026-05-21T10:00:00Z"));
                    assertThat(reloaded.getUpdatedAt()).isEqualTo(Instant.parse("2026-05-21T10:00:00Z"));
                });
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        userRepository.saveAndFlush(user("unique@test.com", UserStatus.ACTIVE));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("unique@test.com", UserStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldEnforceUserStatusConstraint() {
        User saved = userRepository.saveAndFlush(user("status@test.com", UserStatus.ACTIVE));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE users SET status = ? WHERE id = ?",
                "UNKNOWN",
                saved.getId()
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String email, UserStatus status) {
        Instant now = Instant.parse("2026-05-21T10:00:00Z");

        return User.builder()
                .email(email)
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
