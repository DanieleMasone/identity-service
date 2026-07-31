package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.api.mapper.UserMapper;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Version 1 user business operations.
 *
 * <p>Version 1 exposes a minimal stable API for user creation, lookup, and
 * soft deletion.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceV1 {

    private final UserServiceSupport support;
    private final UserMapper userMapper;

    /**
     * Creates a user from the version 1 API request.
     *
     * @param request generated request model validated by the API layer
     * @return generated response model for the created user
     */
    public UserResponseV1 createUser(CreateUserRequestV1 request) {
        User user = userMapper.toEntity(request);
        return userMapper.toV1(support.create(user, request.getPassword()));
    }

    /**
     * Loads a user by identifier.
     *
     * @param id user identifier parsed from the request path
     * @return generated response model for the found user
     */
    @Transactional(readOnly = true)
    public UserResponseV1 getUserById(UUID id) {
        return userMapper.toV1(support.getRequired(id));
    }

    /**
     * Soft-deletes a user by marking the account as inactive.
     *
     * @param id user identifier parsed from the request path
     */
    public void deleteUser(UUID id) {
        User user = support.getRequired(id);
        user.setStatus(UserStatus.INACTIVE);
        support.saveChanged(user);
    }
}
