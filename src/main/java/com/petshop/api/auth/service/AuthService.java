package com.petshop.api.auth.service;

import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.dto.MeResponse;
import com.petshop.api.auth.exception.AuthExceptions.InvalidCredentialsException;
import com.petshop.api.auth.exception.AuthExceptions.InvalidRefreshTokenException;
import com.petshop.api.auth.repository.UserRepository;
import com.petshop.api.auth.security.AuthenticatedUser;
import com.petshop.api.products.service.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Login, token refresh, logout and password change. */
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokens;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final Clock clock;

    /** Compared against when the e-mail is unknown, so response time does not reveal which e-mails exist. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, RefreshTokenService refreshTokens, JwtService jwtService,
                       PasswordEncoder passwordEncoder, LoginRateLimiter rateLimiter, Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokens = refreshTokens;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode("timing-equalizer-not-a-password");
    }

    /** Access token for the body, refresh token for the cookie. */
    public record Session(String accessToken, long expiresIn, String refreshToken, MeResponse user) {
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public Session login(String email, String password, String ip, String userAgent) {
        rateLimiter.checkAllowed(email, ip);

        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        boolean passwordOk = passwordEncoder.matches(password, user != null ? user.getPasswordHash() : dummyHash);

        if (user == null || !passwordOk || !user.isActive()) {
            rateLimiter.recordFailure(email, ip);
            log.info("Login failed");   // no e-mail in the log: it is personal data
            throw new InvalidCredentialsException();
        }

        rateLimiter.recordSuccess(email);
        user.setLastLoginAt(clock.instant());
        log.info("Login succeeded for user {}", user.getPublicId());
        return newSession(user, ip, userAgent);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Session refresh(String rawRefreshToken, String ip, String userAgent) {
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(rawRefreshToken, ip, userAgent);
        User user = rotation.user();
        return new Session(jwtService.issueAccessToken(user), jwtService.accessTokenTtlSeconds(),
                rotation.newRawToken(), MeResponse.of(user));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokens.revoke(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public MeResponse me(AuthenticatedUser principal) {
        return MeResponse.of(load(principal));
    }

    /**
     * Changes the password, ends every other session (all tokens are invalidated) and returns a
     * fresh session for the current device.
     */
    @Transactional
    public Session changePassword(AuthenticatedUser principal, String currentPassword, String newPassword,
                                  String ip, String userAgent) {
        User user = load(principal);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("A senha atual está incorreta.");
        }
        PasswordRules.check(newPassword);
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("A nova senha deve ser diferente da atual.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.invalidateTokens();
        refreshTokens.revokeAll(user);
        log.info("Password changed for user {}", user.getPublicId());
        return newSession(user, ip, userAgent);
    }

    private Session newSession(User user, String ip, String userAgent) {
        return new Session(jwtService.issueAccessToken(user), jwtService.accessTokenTtlSeconds(),
                refreshTokens.issue(user, ip, userAgent), MeResponse.of(user));
    }

    private User load(AuthenticatedUser principal) {
        return userRepository.findById(principal.id()).orElseThrow(InvalidRefreshTokenException::new);
    }
}
