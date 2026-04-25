package com.dmasone.identity.api.controller;

import com.dmasone.identity.api.generated.UsersV2Api;
import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.service.UserServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UsersV2Controller implements UsersV2Api {

    private final UserServiceV2 userService;

    @Override
    public ResponseEntity<UserResponseV2> createUserV2(CreateUserRequestV2 request) {
        return ResponseEntity
                .status(201)
                .body(userService.createUser(request));
    }

    @Override
    public ResponseEntity<UserResponseV2> getUserByIdV2(UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserResponseV2> updateUserV2(UUID id, UpdateUserRequestV2 request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
