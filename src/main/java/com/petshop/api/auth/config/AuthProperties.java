package com.petshop.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * app.auth.* — token lifetimes and cookie settings.
 *
 * @param accessTokenTtl    lifetime of the bearer token kept in the browser's memory
 * @param refreshTokenTtl   lifetime of the httpOnly refresh cookie (= how long a login lasts)
 * @param refreshReuseGrace a just-rotated refresh token is still honoured for this long, so two tabs
 *                          refreshing at the same moment are not mistaken for token theft
 * @param cookieSecure      Secure flag on the refresh cookie (browsers accept it on http://localhost too)
 */
@ConfigurationProperties("app.auth")
public record AuthProperties(
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        Duration refreshReuseGrace,
        Boolean cookieSecure
) {
    public AuthProperties {
        if (accessTokenTtl == null) accessTokenTtl = Duration.ofMinutes(15);
        if (refreshTokenTtl == null) refreshTokenTtl = Duration.ofDays(30);
        if (refreshReuseGrace == null) refreshReuseGrace = Duration.ofSeconds(10);
        if (cookieSecure == null) cookieSecure = true;
    }
}
