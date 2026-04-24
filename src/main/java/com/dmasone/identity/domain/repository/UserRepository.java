package com.dmasone.identity.domain.repository;

import com.dmasone.identity.domain.model.User;
import com.dmasone.identity.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByStatus(UserStatus status);
}
