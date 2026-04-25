package com.dmasone.identity.api.mapper;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseV1 toV1(User user);

    UserResponseV2 toV2(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    User toEntity(CreateUserRequestV1 request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequestV2 request);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
