package com.petshop.api.auth.dto;

import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/** Requests and responses of /api/me/password and /api/users. */
public final class UserDtos {

    private UserDtos() {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotNull Role role,
            /** Temporary: the user must replace it at first login. */
            @NotBlank String temporaryPassword
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(max = 255) String name,
            @NotNull Role role,
            @NotNull UserStatus status
    ) {
    }

    public record ResetPasswordRequest(@NotBlank String temporaryPassword) {
    }

    public record UserResponse(UUID id, String name, String email, Role role, UserStatus status,
                               boolean mustChangePassword, Instant lastLoginAt) {

        public static UserResponse of(User user) {
            return new UserResponse(user.getPublicId(), user.getName(), user.getEmail(), user.getRole(),
                    user.getStatus(), user.isMustChangePassword(), user.getLastLoginAt());
        }
    }
}
