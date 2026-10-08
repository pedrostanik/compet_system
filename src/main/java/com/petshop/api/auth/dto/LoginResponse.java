package com.petshop.api.auth.dto;

/**
 * Returned by login, refresh and password change. The access token is kept in memory by the
 * web app (never in localStorage); the refresh token travels only as an httpOnly cookie.
 */
public record LoginResponse(String accessToken, long expiresIn, MeResponse user) {}
