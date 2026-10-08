package com.petshop.api.auth.security;

import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/** The logged-in user, as stored in the SecurityContext by JwtFilter. */
public record AuthenticatedUser(Long id, UUID publicId, String email, String name, Role role,
                                boolean mustChangePassword) {

    public static AuthenticatedUser of(User user) {
        return new AuthenticatedUser(user.getId(), user.getPublicId(), user.getEmail(), user.getName(),
                user.getRole(), user.isMustChangePassword());
    }

    public static Optional<AuthenticatedUser> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AuthenticatedUser user
                ? Optional.of(user)
                : Optional.empty();
    }
}
