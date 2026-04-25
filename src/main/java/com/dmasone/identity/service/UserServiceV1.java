package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.UserResponseV1;

import java.util.UUID;

public interface UserServiceV1 {

    UserResponseV1 createUser(CreateUserRequestV1 request);

    UserResponseV1 getUserById(UUID id);

    void deleteUser(UUID id);
}
