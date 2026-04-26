package com.dmasone.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security-related bean configuration.
 *
 * <p>The service currently focuses on identity data management rather than full
 * authentication, but passwords are still stored as BCrypt hashes to avoid
 * persisting raw credentials.</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * Provides the password hashing strategy used when users are created.
     *
     * @return BCrypt-based password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
