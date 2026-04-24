package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV1;
import com.dmasone.identity.api.dto.UserResponseV1;
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
public class UserServiceV1Impl implements UserServiceV1 {

    private final UserRepository userRepository;

    @Override
    public UserResponseV1 createUser(CreateUserRequestV1 request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hash(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(user);

        return mapToResponse(saved);
    }

    @Override
    public UserResponseV1 getUserById(String id) {
        User user = userRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapToResponse(user);
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // soft delete
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    // ===== helpers =====

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new UserNotFoundException("Invalid user ID");
        }
    }

    private UserResponseV1 mapToResponse(User user) {
        return UserResponseV1.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String hash(String password) {
        return "hashed_" + password;
    }
}
