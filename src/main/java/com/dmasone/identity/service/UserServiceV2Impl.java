package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV2;
import com.dmasone.identity.api.dto.UpdateUserRequestV2;
import com.dmasone.identity.api.dto.UserResponseV2;
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

    @Override
    public UserResponseV2 createUser(CreateUserRequestV2 request) {

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

        return mapToResponse(saved, request.getFirstName(), request.getLastName());
    }

    @Override
    public UserResponseV2 getUserById(String id) {
        User user = userRepository.findById(parseUUID(id))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapToResponse(user, null, null);
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

        return mapToResponse(saved, request.getFirstName(), request.getLastName());
    }

    // ===== helpers =====

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new UserNotFoundException("Invalid user ID");
        }
    }

    private UserResponseV2 mapToResponse(User user, String firstName, String lastName) {
        return UserResponseV2.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String hash(String password) {
        return "hashed_" + password;
    }
}
