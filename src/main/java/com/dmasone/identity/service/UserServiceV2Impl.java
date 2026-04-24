package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV2;
import com.dmasone.identity.api.dto.UpdateUserRequestV2;
import com.dmasone.identity.api.dto.UserResponseV2;
import com.dmasone.identity.api.mapper.UserMapper;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceV2Impl implements UserServiceV2 {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseV2 createUser(CreateUserRequestV2 request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(hash(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        return userMapper.toV2(saved);
    }

    @Override
    public UserResponseV2 getUserById(String id) {

        User user = userRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toV2(user);
    }

    @Override
    public UserResponseV2 updateUser(String id, UpdateUserRequestV2 request) {

        User user = userRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        return userMapper.toV2(saved);
    }

    // ===== helpers =====

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new UserNotFoundException("Invalid user ID");
        }
    }

    private String hash(String password) {
        return "hashed_" + password;
    }
}