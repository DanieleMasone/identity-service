package com.dmasone.identity.service;

import com.dmasone.identity.api.dto.CreateUserRequestV1;
import com.dmasone.identity.api.dto.UserResponseV1;

public interface UserServiceV1 {

    UserResponseV1 createUser(CreateUserRequestV1 request);

    UserResponseV1 getUserById(String id);

    void deleteUser(String id);
}
