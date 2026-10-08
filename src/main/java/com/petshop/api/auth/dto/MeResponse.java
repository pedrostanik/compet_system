package com.petshop.api.auth.dto;

import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;

import java.util.UUID;

/** The logged-in user. Phase 2 adds the tenant; Phase 3 the plan, limits and usage. */
public record MeResponse(UUID id, String name, String email, Role role, boolean mustChangePassword) {

    public static MeResponse of(User user) {
        return new MeResponse(user.getPublicId(), user.getName(), user.getEmail(), user.getRole(),
                user.isMustChangePassword());
    }
}
