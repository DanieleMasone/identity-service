package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;

import java.util.UUID;

/**
 * Business contract for version 2 user operations.
 *
 * <p>Version 2 extends the user model with profile fields and partial updates
 * while preserving the behavior of earlier API versions.</p>
 */
public interface UserServiceV2 {

    /**
     * Creates a user from the version 2 API request.
     *
     * @param request generated request model validated by the API layer
     * @return generated response model for the created user
     */
    UserResponseV2 createUser(CreateUserRequestV2 request);

    /**
     * Loads a user by identifier.
     *
     * @param id user identifier parsed from the request path
     * @return generated response model for the found user
     */
    UserResponseV2 getUserById(UUID id);

    /**
     * Applies a partial update to a version 2 user profile.
     *
     * @param id user identifier parsed from the request path
     * @param request generated partial-update request
     * @return generated response model after persistence
     */
    UserResponseV2 updateUser(UUID id, UpdateUserRequestV2 request);
}
