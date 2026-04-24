package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV2;
import com.dmasone.identity.api.dto.UpdateUserRequestV2;
import com.dmasone.identity.api.dto.UserResponseV2;

public interface UserServiceV2 {

    UserResponseV2 createUser(CreateUserRequestV2 request);

    UserResponseV2 getUserById(String id);

    UserResponseV2 updateUser(String id, UpdateUserRequestV2 request);
}
