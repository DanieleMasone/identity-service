package com.dmasone.identity.api.controller;

import com.dmasone.identity.api.generated.UsersV1Api;
import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * HTTP adapter for the first version of the users API.
 *
 * <p>The controller implements the OpenAPI-generated contract and delegates all
 * business decisions to {@link UserServiceV1}. Version 1 intentionally exposes a
 * compact user representation for backward compatibility.</p>
 */
@RestController
@RequiredArgsConstructor
public class UsersV1Controller implements UsersV1Api {

    private final UserServiceV1 userService;

    @Override
    public ResponseEntity<UserResponseV1> createUserV1(CreateUserRequestV1 request) {
        return ResponseEntity
                .status(201)
                .body(userService.createUser(request));
    }

    @Override
    public ResponseEntity<UserResponseV1> getUserByIdV1(UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<Void> deleteUserV1(UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
