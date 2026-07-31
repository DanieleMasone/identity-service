package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.api.mapper.UserMapper;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Version 2 user business operations.
 *
 * <p>Version 2 extends the user model with profile fields and partial updates
 * while preserving the behavior of earlier API versions.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceV2 {

    private final UserServiceSupport support;
    private final UserMapper userMapper;

    /**
     * Creates a user from the version 2 API request.
     *
     * @param request generated request model validated by the API layer
     * @return generated response model for the created user
     */
    public UserResponseV2 createUser(CreateUserRequestV2 request) {
        User user = userMapper.toEntity(request);
        return userMapper.toV2(support.create(user, request.getPassword()));
    }

    /**
     * Loads a user by identifier.
     *
     * @param id user identifier parsed from the request path
     * @return generated response model for the found user
     */
    @Transactional(readOnly = true)
    public UserResponseV2 getUserById(UUID id) {
        return userMapper.toV2(support.getRequired(id));
    }

    /**
     * Applies a partial update to a version 2 user profile.
     *
     * @param id user identifier parsed from the request path
     * @param request generated partial-update request
     * @return generated response model after persistence
     */
    public UserResponseV2 updateUser(UUID id, UpdateUserRequestV2 request) {
        User user = support.getRequired(id);
        boolean changed = false;

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            changed = true;
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            changed = true;
        }

        if (request.getStatus() != null) {
            user.setStatus(toDomainStatus(request.getStatus()));
            changed = true;
        }

        return userMapper.toV2(changed ? support.saveChanged(user) : user);
    }

    private UserStatus toDomainStatus(com.dmasone.identity.api.generated.model.UserStatus status) {
        return UserStatus.valueOf(status.getValue());
    }
}
