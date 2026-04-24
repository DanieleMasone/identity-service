package com.dmasone.identity.api.mapper;

import com.dmasone.identity.api.dto.*;
import com.dmasone.identity.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ===== ENTITY -> RESPONSE V1 =====
    UserResponseV1 toV1(User user);

    // ===== ENTITY -> RESPONSE V2 =====
    UserResponseV2 toV2(User user);

    // ===== REQUEST V1 -> ENTITY =====
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequestV1 request);

    // ===== REQUEST V2 -> ENTITY =====
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequestV2 request);
}
