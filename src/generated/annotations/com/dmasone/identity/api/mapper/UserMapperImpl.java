package com.dmasone.identity.api.mapper;

import com.dmasone.identity.api.generated.model.CreateUserRequestV1;
import com.dmasone.identity.api.generated.model.CreateUserRequestV2;
import com.dmasone.identity.api.generated.model.UserResponseV1;
import com.dmasone.identity.api.generated.model.UserResponseV2;
import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseV1 toV1(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseV1 userResponseV1 = new UserResponseV1();

        userResponseV1.setId( user.getId() );
        userResponseV1.setEmail( user.getEmail() );
        userResponseV1.setStatus( userStatusToUserStatus( user.getStatus() ) );
        userResponseV1.setCreatedAt( map( user.getCreatedAt() ) );

        return userResponseV1;
    }

    @Override
    public UserResponseV2 toV2(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseV2 userResponseV2 = new UserResponseV2();

        userResponseV2.setId( user.getId() );
        userResponseV2.setEmail( user.getEmail() );
        userResponseV2.setFirstName( user.getFirstName() );
        userResponseV2.setLastName( user.getLastName() );
        userResponseV2.setStatus( userStatusToUserStatus( user.getStatus() ) );
        userResponseV2.setCreatedAt( map( user.getCreatedAt() ) );
        userResponseV2.setUpdatedAt( map( user.getUpdatedAt() ) );

        return userResponseV2;
    }

    @Override
    public User toEntity(CreateUserRequestV1 request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );

        return user.build();
    }

    @Override
    public User toEntity(CreateUserRequestV2 request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );

        return user.build();
    }

    protected com.dmasone.identity.api.generated.model.UserStatus userStatusToUserStatus(UserStatus userStatus) {
        if ( userStatus == null ) {
            return null;
        }

        com.dmasone.identity.api.generated.model.UserStatus userStatus1;

        switch ( userStatus ) {
            case ACTIVE: userStatus1 = com.dmasone.identity.api.generated.model.UserStatus.ACTIVE;
            break;
            case INACTIVE: userStatus1 = com.dmasone.identity.api.generated.model.UserStatus.INACTIVE;
            break;
            case SUSPENDED: userStatus1 = com.dmasone.identity.api.generated.model.UserStatus.SUSPENDED;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + userStatus );
        }

        return userStatus1;
    }
}
