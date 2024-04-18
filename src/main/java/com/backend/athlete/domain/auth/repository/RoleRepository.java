package com.backend.athlete.domain.auth.repository;

import com.backend.athlete.domain.user.domain.Role;
import com.backend.athlete.domain.user.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleType name);

}
