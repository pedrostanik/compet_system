package com.petshop.api.auth.security;

import com.petshop.api.auth.exception.AuthExceptions.PasswordChangeRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * A user logged in with a temporary password (set by an owner/admin) may only see who they are
 * and change the password; everything else answers 403 with code PASSWORD_CHANGE_REQUIRED,
 * which the web app turns into a redirect to the "new password" screen.
 */
@Configuration
public class PasswordChangeInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/me", "/api/me/**", "/api/enums/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser.current()
                .filter(AuthenticatedUser::mustChangePassword)
                .ifPresent(user -> {
                    throw new PasswordChangeRequiredException();
                });
        return true;
    }
}
