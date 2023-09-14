package com.schumaker.api.security.model.repository;

import com.schumaker.api.security.model.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    UserDetails findByEmail(String username);
}
