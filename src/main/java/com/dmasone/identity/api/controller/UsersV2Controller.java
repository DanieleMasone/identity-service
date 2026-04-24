package com.dmasone.identity.api.controller;

import com.dmasone.identity.api.dto.CreateUserRequestV2;
import com.dmasone.identity.api.dto.UpdateUserRequestV2;
import com.dmasone.identity.api.dto.UserResponseV2;
import com.dmasone.identity.api.generated.UsersApiV2;
import com.dmasone.identity.api.generated.model.*;
import com.dmasone.identity.service.UserServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UsersV2Controller implements UsersApiV2 {

    private final UserServiceV2 userService;

    @Override
    public ResponseEntity<UserResponseV2> createUserV2(CreateUserRequestV2 request) {
        return ResponseEntity
                .status(201)
                .body(userService.createUser(request));
    }

    @Override
    public ResponseEntity<UserResponseV2> getUserByIdV2(String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserResponseV2> updateUserV2(String id, UpdateUserRequestV2 request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
