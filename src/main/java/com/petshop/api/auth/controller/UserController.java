package com.petshop.api.auth.controller;

import com.petshop.api.auth.dto.UserDtos.CreateUserRequest;
import com.petshop.api.auth.dto.UserDtos.ResetPasswordRequest;
import com.petshop.api.auth.dto.UserDtos.UpdateUserRequest;
import com.petshop.api.auth.dto.UserDtos.UserResponse;
import com.petshop.api.auth.security.AuthenticatedUser;
import com.petshop.api.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Account management — OWNER and ADMIN only (SecurityConfig). */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> list() {
        return userService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.create(request, actor);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.update(id, request, actor);
    }

    @PostMapping("/{id}/reset-password")
    public UserResponse resetPassword(@PathVariable UUID id, @Valid @RequestBody ResetPasswordRequest request,
                                      @AuthenticationPrincipal AuthenticatedUser actor) {
        return userService.resetPassword(id, request.temporaryPassword(), actor);
    }
}
