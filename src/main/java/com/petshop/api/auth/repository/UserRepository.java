package com.petshop.api.auth.repository;

import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE lower(u.email) = lower(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT count(u) > 0 FROM User u WHERE lower(u.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    Optional<User> findByPublicId(UUID publicId);

    long countByRoleAndStatus(Role role, UserStatus status);

    List<User> findAllByOrderByNameAsc();
}
