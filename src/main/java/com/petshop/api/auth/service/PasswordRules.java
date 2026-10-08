package com.petshop.api.auth.service;

import com.petshop.api.products.service.BusinessException;

import java.nio.charset.StandardCharsets;

/** Password requirements, checked on create, reset and change. */
public final class PasswordRules {

    public static final int MIN_LENGTH = 8;
    /** BCrypt only uses the first 72 bytes; longer input is rejected by Spring Security. */
    public static final int MAX_BYTES = 72;

    private PasswordRules() {
    }

    public static void check(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new BusinessException("A senha deve ter pelo menos " + MIN_LENGTH + " caracteres.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new BusinessException("A senha é longa demais (máximo de " + MAX_BYTES + " bytes).");
        }
        if (password.isBlank()) {
            throw new BusinessException("A senha não pode ser só espaços.");
        }
    }
}
