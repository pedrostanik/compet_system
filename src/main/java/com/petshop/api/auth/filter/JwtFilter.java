package com.petshop.api.auth.filter;

import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.repository.UserRepository;
import com.petshop.api.auth.security.AuthenticatedUser;
import com.petshop.api.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates "Authorization: Bearer <access token>". The user is reloaded on every request
 * (3 or so users per shop — cheap), so a disabled account, a role change or a bumped
 * token_version (password change/reset) takes effect immediately, not when the token expires.
 * Requests without a valid token stay anonymous; SecurityConfig decides what they may reach.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.parse(header.substring(7))
                    .flatMap(claims -> userRepository.findByPublicId(claims.userPublicId())
                            .filter(User::isActive)
                            .filter(user -> user.getTokenVersion() == claims.tokenVersion()))
                    .ifPresent(user -> {
                        var auth = new UsernamePasswordAuthenticationToken(
                                AuthenticatedUser.of(user), null,
                                List.of(new SimpleGrantedAuthority(user.getRole().authority())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        filterChain.doFilter(request, response);
    }
}
