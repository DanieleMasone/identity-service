package com.dmasone.identity.integration;

import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository integration tests backed by real PostgreSQL.
 */
class UserRepositoryIT extends PostgresIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByEmail() {
        User saved = userRepository.saveAndFlush(user("repository@test.com", UserStatus.ACTIVE));

        assertThat(userRepository.findByEmail("repository@test.com"))
                .get()
                .extracting(User::getId)
                .isEqualTo(saved.getId());
        assertThat(userRepository.existsByEmail("repository@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("missing@test.com")).isFalse();
    }

    @Test
    void shouldFindUsersByStatus() {
        User active = userRepository.save(user("active@test.com", UserStatus.ACTIVE));
        userRepository.save(user("inactive@test.com", UserStatus.INACTIVE));
        userRepository.flush();

        List<User> activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);

        assertThat(activeUsers)
                .extracting(User::getId)
                .containsExactly(active.getId());
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        userRepository.saveAndFlush(user("unique@test.com", UserStatus.ACTIVE));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("unique@test.com", UserStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
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
