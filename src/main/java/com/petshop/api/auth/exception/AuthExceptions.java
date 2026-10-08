package com.petshop.api.auth.exception;

import lombok.Getter;

/** Authentication failures, mapped to HTTP responses in GlobalExceptionHandler. */
public final class AuthExceptions {

    private AuthExceptions() {
    }

    /** Wrong e-mail or password, or a disabled account — deliberately indistinguishable (401). */
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("E-mail ou senha incorretos.");
        }
    }

    /** Missing, expired, revoked or unknown refresh token: the user must log in again (401). */
    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException() {
            super("Sessão expirada. Entre novamente.");
        }
    }

    /** Too many failed logins (429); retryAfterSeconds goes into the Retry-After header. */
    @Getter
    public static class TooManyAttemptsException extends RuntimeException {
        private final long retryAfterSeconds;

        public TooManyAttemptsException(long retryAfterSeconds) {
            super("Muitas tentativas de login. Tente novamente em "
                    + Math.max(1, (retryAfterSeconds + 59) / 60) + " minuto(s).");
            this.retryAfterSeconds = retryAfterSeconds;
        }
    }

    /** The user logged in with a temporary password and must set their own first (403). */
    public static class PasswordChangeRequiredException extends RuntimeException {
        public static final String CODE = "PASSWORD_CHANGE_REQUIRED";

        public PasswordChangeRequiredException() {
            super("Defina uma nova senha para continuar.");
        }
    }
}
