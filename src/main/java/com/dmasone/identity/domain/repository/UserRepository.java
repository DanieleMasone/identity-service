package com.dmasone.identity.domain.repository;

import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
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
     * Finds a user by email address.
     *
     * @param email unique email address used as a login identifier
     * @return matching user, if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an email is already assigned to another user.
     *
     * @param email email address to check
     * @return {@code true} when the email is already persisted
     */
    boolean existsByEmail(String email);

    /**
     * Lists users matching a lifecycle status.
     *
     * @param status user lifecycle status
     * @return users currently in the requested status
     */
    List<User> findAllByStatus(UserStatus status);
}
