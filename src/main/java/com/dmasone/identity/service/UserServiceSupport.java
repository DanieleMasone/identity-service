package com.dmasone.identity.service;

import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Shared persistence-aware rules used by both API versions.
 */
@Component
@RequiredArgsConstructor
class UserServiceSupport {

    private static final String EMAIL_EXISTS_MESSAGE = "Email already exists";
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    User create(User user, String rawPassword) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(EMAIL_EXISTS_MESSAGE);
        }

        Instant now = currentTimestamp();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (isUniqueConstraintViolation(exception)) {
                throw new EmailAlreadyExistsException(EMAIL_EXISTS_MESSAGE, exception);
            }
            throw exception;
        }
    }

    User getRequired(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    User saveChanged(User user) {
        user.setUpdatedAt(currentTimestamp());
        return userRepository.save(user);
    }

    private Instant currentTimestamp() {
        // PostgreSQL stores timestamps at microsecond precision; matching it keeps
        // write responses stable when the same entity is read back.
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private boolean isUniqueConstraintViolation(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }

            Throwable cause = current.getCause();
            current = cause == current ? null : cause;
        }

        return false;
    }
}
