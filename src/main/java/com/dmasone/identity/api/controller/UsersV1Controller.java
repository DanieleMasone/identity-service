package com.dmasone.identity.api.controller;

import com.dmasone.identity.api.dto.CreateUserRequestV1;
import com.dmasone.identity.api.dto.UserResponseV1;
import com.dmasone.identity.api.generated.UsersApi;
import com.dmasone.identity.api.generated.model.*;
import com.dmasone.identity.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UsersV1Controller implements UsersApi {

    private final UserServiceV1 userService;

    @Override
    public ResponseEntity<UserResponseV1> createUserV1(CreateUserRequestV1 request) {
        return ResponseEntity
                .status(201)
                .body(userService.createUser(request));
    }

    @Override
    public ResponseEntity<UserResponseV1> getUserByIdV1(String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<Void> deleteUserV1(String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
