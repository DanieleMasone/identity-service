package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.api.mapper.UserMapper;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import com.dmasone.identity.domain.repository.UserRepository;
import com.dmasone.identity.infrastructure.exception.EmailAlreadyExistsException;
import com.dmasone.identity.infrastructure.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceV2Impl implements UserServiceV2 {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseV2 createUser(CreateUserRequestV2 request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        return userMapper.toV2(saved);
    }

    @Override
    public UserResponseV2 getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toV2(user);
    }

    @Override
    public UserResponseV2 updateUser(UUID id, UpdateUserRequestV2 request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getStatus() != null) {
            user.setStatus(toDomainStatus(request.getStatus()));
        }

        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        return userMapper.toV2(saved);
    }

    private UserStatus toDomainStatus(com.dmasone.identity.api.generated.model.UserStatus status) {
        return UserStatus.valueOf(status.getValue());
    }
}
