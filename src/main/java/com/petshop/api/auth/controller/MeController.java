package com.petshop.api.auth.controller;

import com.petshop.api.auth.dto.LoginResponse;
import com.petshop.api.auth.dto.MeResponse;
import com.petshop.api.auth.dto.UserDtos.ChangePasswordRequest;
import com.petshop.api.auth.security.AuthenticatedUser;
import com.petshop.api.auth.service.AuthService;
import com.petshop.api.auth.service.AuthService.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** The logged-in user: who am I, and change my password. */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService;
    private final RefreshCookie refreshCookie;

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.me(user);
    }

    /** Ends all other sessions; this device gets a new token pair. */
    @PostMapping("/password")
    public ResponseEntity<LoginResponse> changePassword(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Valid @RequestBody ChangePasswordRequest request,
                                                        HttpServletRequest http) {
        Session session = authService.changePassword(user, request.currentPassword(), request.newPassword(),
                http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT));
        return ResponseEntity.ok()
                .header(RefreshCookie.header(), refreshCookie.set(session.refreshToken()))
                .body(new LoginResponse(session.accessToken(), session.expiresIn(), session.user()));
    }
}
