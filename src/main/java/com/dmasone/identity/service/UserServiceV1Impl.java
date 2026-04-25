package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
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
public class UserServiceV1Impl implements UserServiceV1 {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseV1 createUser(CreateUserRequestV1 request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        return userMapper.toV1(saved);
    }

    @Override
    public UserResponseV1 getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toV1(user);
    }

    @Override
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }
}
