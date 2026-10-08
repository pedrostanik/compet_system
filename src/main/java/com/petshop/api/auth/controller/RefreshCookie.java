package com.petshop.api.auth.controller;

import com.petshop.api.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * The refresh token cookie: httpOnly (JavaScript cannot read it, so an XSS cannot steal it),
 * Secure, SameSite=Strict, and only sent to /api/auth — not with every API call.
 */
@Component
@RequiredArgsConstructor
public class RefreshCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/api/auth";

    private final AuthProperties props;

    public String set(String rawToken) {
        return build(rawToken, props.refreshTokenTtl());
    }

    public String clear() {
        return build("", Duration.ZERO);
    }

    private String build(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(props.cookieSecure())
                .sameSite("Strict")
                .path(PATH)
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public static String header() {
        return HttpHeaders.SET_COOKIE;
    }
}
