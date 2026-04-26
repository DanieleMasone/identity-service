package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;

import java.util.UUID;

/**
 * Business contract for version 1 user operations.
 *
 * <p>Version 1 exposes a minimal stable API for user creation, lookup, and
 * soft deletion.</p>
 */
public interface UserServiceV1 {

    /**
     * Creates a user from the version 1 API request.
     *
     * @param request generated request model validated by the API layer
     * @return generated response model for the created user
     */
    UserResponseV1 createUser(CreateUserRequestV1 request);

    /**
     * Loads a user by identifier.
     *
     * @param id user identifier parsed from the request path
     * @return generated response model for the found user
     */
    UserResponseV1 getUserById(UUID id);

    /**
     * Soft-deletes a user by marking the account as inactive.
     *
     * @param id user identifier parsed from the request path
     */
    void deleteUser(UUID id);
}
