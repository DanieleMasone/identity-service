package com.dmasone.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Identity Service application.
 *
 * <p>The application is intentionally placed in the root package so Spring Boot can
 * discover controllers, services, repositories, and infrastructure components
 * across the whole {@code com.dmasone.identity} namespace.</p>
 */
@SpringBootApplication
public class IdentityServiceApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed by the runtime
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
