package com.petshop.api.auth.controller;

import com.petshop.api.auth.dto.LoginRequest;
import com.petshop.api.auth.dto.LoginResponse;
import com.petshop.api.auth.service.AuthService;
import com.petshop.api.auth.service.AuthService.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Public endpoints: log in, renew the access token from the refresh cookie, log out. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshCookie refreshCookie;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        Session session = authService.login(request.email(), request.password(),
                http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT));
        return withSession(session);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken,
            HttpServletRequest http) {
        Session session = authService.refresh(refreshToken, http.getRemoteAddr(), http.getHeader(HttpHeaders.USER_AGENT));
        return withSession(session);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().header(RefreshCookie.header(), refreshCookie.clear()).build();
    }

    ResponseEntity<LoginResponse> withSession(Session session) {
        return ResponseEntity.ok()
                .header(RefreshCookie.header(), refreshCookie.set(session.refreshToken()))
                .body(new LoginResponse(session.accessToken(), session.expiresIn(), session.user()));
    }
}
