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

/**
 * Maps between generated API models and the internal user entity.
 *
 * <p>MapStruct generates the implementation at build time, keeping the mapping
 * explicit and compile-time checked without hand-written boilerplate.</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a domain user into the version 1 API response.
     *
     * @param user persisted user entity
     * @return user representation exposed by the v1 API
     */
    UserResponseV1 toV1(User user);

    /**
     * Converts a domain user into the version 2 API response.
     *
     * @param user persisted user entity
     * @return user representation exposed by the v2 API
     */
    UserResponseV2 toV2(User user);

    /**
     * Converts a v1 creation request into a new user entity.
     *
     * <p>Generated identifiers, timestamps, status, and password hash are set by
     * the service layer because they belong to application behavior, not request
     * mapping.</p>
     *
     * @param request validated v1 API request
     * @return a new user entity ready for service enrichment
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    User toEntity(CreateUserRequestV1 request);

    /**
     * Converts a v2 creation request into a new user entity.
     *
     * @param request validated v2 API request
     * @return a new user entity ready for service enrichment
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequestV2 request);

    /**
     * Converts persistence timestamps to API-friendly offset timestamps.
     *
     * @param instant timestamp stored by the domain model
     * @return UTC offset timestamp for generated OpenAPI responses
     */
    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
