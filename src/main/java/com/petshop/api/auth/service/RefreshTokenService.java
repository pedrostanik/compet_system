package com.petshop.api.auth.service;

import com.petshop.api.auth.config.AuthProperties;
import com.petshop.api.auth.domain.RefreshToken;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.exception.AuthExceptions.InvalidRefreshTokenException;
import com.petshop.api.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Opaque, rotating refresh tokens. The raw value (256 random bits) only exists in the user's
 * httpOnly cookie; the database keeps its SHA-256. Every refresh revokes the presented token and
 * issues a new one. Presenting an already-revoked token again means it was copied — every
 * session of that user is then revoked.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final AuthProperties props;
    private final Clock clock;

    public record Rotation(User user, String newRawToken) {
    }

    @Transactional
    public String issue(User user, String ip, String userAgent) {
        return create(user, ip, userAgent).raw();
    }

    // noRollbackFor: on token reuse, revoking every session must be committed even though we throw.
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Rotation rotate(String rawToken, String ip, String userAgent) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        RefreshToken token = repository.findForUpdateByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        User user = token.getUser();
        Instant now = clock.instant();

        if (!user.isActive()) {
            throw new InvalidRefreshTokenException();
        }
        if (token.isRevoked()) {
            boolean justRotated = token.getReplacedBy() != null
                    && token.getRevokedAt().isAfter(now.minus(props.refreshReuseGrace()));
            if (justRotated) {
                // Another tab refreshed with this token a moment ago: give this one its own token.
                return new Rotation(user, create(user, ip, userAgent).raw());
            }
            log.warn("Refresh token reuse detected for user {}: revoking all sessions", user.getPublicId());
            repository.revokeAllForUser(user, now);
            throw new InvalidRefreshTokenException();
        }
        if (token.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException();
        }

        Created next = create(user, ip, userAgent);
        token.setRevokedAt(now);
        token.setReplacedBy(next.entity().getId());
        return new Rotation(user, next.raw());
    }

    /** Logout: revokes the given token; unknown or already revoked tokens are ignored. */
    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(rawToken))
                .filter(t -> !t.isRevoked())
                .ifPresent(t -> t.setRevokedAt(clock.instant()));
    }

    /** Ends every session of the user (password change, reset, account disabled). */
    @Transactional
    public void revokeAll(User user) {
        repository.revokeAllForUser(user, clock.instant());
    }

    private record Created(RefreshToken entity, String raw) {
    }

    private Created create(User user, String ip, String userAgent) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(clock.instant().plus(props.refreshTokenTtl()));
        token.setIp(truncate(ip, 45));
        token.setUserAgent(truncate(userAgent, 255));
        return new Created(repository.save(token), raw);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
