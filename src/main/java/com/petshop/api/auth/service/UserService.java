package com.petshop.api.auth.service;

import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.domain.UserStatus;
import com.petshop.api.auth.dto.UserDtos.CreateUserRequest;
import com.petshop.api.auth.dto.UserDtos.UpdateUserRequest;
import com.petshop.api.auth.dto.UserDtos.UserResponse;
import com.petshop.api.auth.repository.UserRepository;
import com.petshop.api.auth.security.AuthenticatedUser;
import com.petshop.api.products.service.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Account management by OWNER/ADMIN (URL access is restricted in SecurityConfig). Rules:
 * only an OWNER may create, edit or reset an OWNER account or grant the OWNER role; nobody
 * changes their own role or status (use "change password" for yourself); the last active
 * OWNER can never be demoted or disabled.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAllByOrderByNameAsc().stream().map(UserResponse::of).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request, AuthenticatedUser actor) {
        requireMayAssign(actor, request.role());
        String email = request.email().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Já existe um usuário com este e-mail.");
        }
        PasswordRules.check(request.temporaryPassword());

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(request.role());
        user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
        user.setMustChangePassword(true);
        userRepository.save(user);
        log.info("User {} created with role {} by {}", user.getPublicId(), user.getRole(), actor.publicId());
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request, AuthenticatedUser actor) {
        User user = find(id);
        requireMayManage(actor, user);

        boolean roleOrStatusChanges = user.getRole() != request.role() || user.getStatus() != request.status();
        if (roleOrStatusChanges && user.getId().equals(actor.id())) {
            throw new BusinessException("Você não pode alterar o seu próprio perfil de acesso ou status.");
        }
        if (request.role() != user.getRole()) {
            requireMayAssign(actor, request.role());
        }
        boolean stopsBeingActiveOwner = user.getRole() == Role.OWNER && user.isActive()
                && (request.role() != Role.OWNER || request.status() != UserStatus.ACTIVE);
        if (stopsBeingActiveOwner && userRepository.countByRoleAndStatus(Role.OWNER, UserStatus.ACTIVE) <= 1) {
            throw new BusinessException("É preciso manter pelo menos um proprietário (OWNER) ativo.");
        }

        user.setName(request.name().trim());
        user.setRole(request.role());
        if (user.getStatus() != request.status()) {
            user.setStatus(request.status());
            if (request.status() == UserStatus.DISABLED) {
                // Logged out everywhere right away (JwtFilter also rejects disabled users).
                user.invalidateTokens();
                refreshTokens.revokeAll(user);
            }
        }
        log.info("User {} updated by {}: role={}, status={}", user.getPublicId(), actor.publicId(),
                user.getRole(), user.getStatus());
        return UserResponse.of(user);
    }

    /** Sets a temporary password; the user is logged out everywhere and must choose a new one. */
    @Transactional
    public UserResponse resetPassword(UUID id, String temporaryPassword, AuthenticatedUser actor) {
        User user = find(id);
        requireMayManage(actor, user);
        if (user.getId().equals(actor.id())) {
            throw new BusinessException("Para a sua própria senha, use \"Alterar senha\".");
        }
        PasswordRules.check(temporaryPassword);

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.invalidateTokens();
        refreshTokens.revokeAll(user);
        log.info("Password of user {} reset by {}", user.getPublicId(), actor.publicId());
        return UserResponse.of(user);
    }

    private User find(UUID id) {
        return userRepository.findByPublicId(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private static void requireMayManage(AuthenticatedUser actor, User target) {
        if (target.getRole() == Role.OWNER && actor.role() != Role.OWNER) {
            throw new AccessDeniedException("Only an OWNER can manage OWNER accounts");
        }
    }

    private static void requireMayAssign(AuthenticatedUser actor, Role role) {
        if (role == Role.OWNER && actor.role() != Role.OWNER) {
            throw new AccessDeniedException("Only an OWNER can grant the OWNER role");
        }
    }
}
