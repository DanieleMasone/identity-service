package com.dmasone.identity.service;

import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UpdateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV2;

import java.util.UUID;

public interface UserServiceV2 {

    UserResponseV2 createUser(CreateUserRequestV2 request);

    UserResponseV2 getUserById(UUID id);

    UserResponseV2 updateUser(UUID id, UpdateUserRequestV2 request);
}
