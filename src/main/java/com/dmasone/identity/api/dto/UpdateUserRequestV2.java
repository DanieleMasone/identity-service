package com.dmasone.identity.api.dto;

import com.dmasone.identity.domain.model.UserStatus;
import lombok.Data;

@Data
public class UpdateUserRequestV2 {

    private String firstName;
    private String lastName;
    private UserStatus status;
}
