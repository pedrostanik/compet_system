package com.petshop.api.auth.service;

import com.petshop.api.auth.config.AuthProperties;
import com.petshop.api.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Short-lived access tokens (HMAC-SHA, JWT_SECRET). Claims: sub = user public id, ver = token
 * version; role and email are informative only — JwtFilter reloads the user on every request,
 * so a role change or a disabled account takes effect immediately.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey key;
    private final AuthProperties props;
    private final Clock clock;

    public JwtService(@Value("${jwt.secret}") String secret, AuthProperties props, Clock clock) {
        // hmacShaKeyFor rejects secrets shorter than 256 bits: a weak JWT_SECRET fails at startup.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.props = props;
        this.clock = clock;
    }

    public String issueAccessToken(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(user.getPublicId().toString())
                .id(UUID.randomUUID().toString())
                .claim("ver", user.getTokenVersion())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.accessTokenTtl())))
                .signWith(key)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return props.accessTokenTtl().toSeconds();
    }

    /** The token's user and version, or empty if the token is invalid, expired or malformed. */
    public Optional<AccessTokenClaims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Integer version = claims.get("ver", Integer.class);
            if (version == null) {
                return Optional.empty();   // tokens from before Phase 1 carry no version
            }
            return Optional.of(new AccessTokenClaims(UUID.fromString(claims.getSubject()), version));
        } catch (Exception e) {
            // Only the type: the message can contain token content.
            log.debug("Access token rejected: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public record AccessTokenClaims(UUID userPublicId, int tokenVersion) {
    }
}
