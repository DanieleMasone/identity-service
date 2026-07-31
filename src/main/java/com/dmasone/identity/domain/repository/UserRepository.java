package com.dmasone.identity.domain.repository;

import com.dmasone.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Persistence boundary for user records.
 *
 * <p>Spring Data JPA provides the implementation at runtime. The custom query
 * methods express repository capabilities used by the service layer without
 * leaking persistence details into API controllers.</p>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Checks whether an email is already assigned to another user.
     *
     * @param email email address to check
     * @return {@code true} when the email is already persisted
     */
    boolean existsByEmail(String email);
}
