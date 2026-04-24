package com.dmasone.identity.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;


@Data
@Builder
public class UserResponseV2 {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
