package com.petshop.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.auth.filter.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Who may call what. Roles (see auth.domain.Role):
 * <pre>
 *   /api/auth/**, /api/enums/**        anyone
 *   /api/users/**                      OWNER, ADMIN
 *   /api/receipts/**, /api/report/**   read: OWNER, ADMIN, VIEWER — write: OWNER, ADMIN   (no STAFF)
 *   everything else under /api          read: any logged-in user — write: OWNER, ADMIN, STAFF (no VIEWER)
 * </pre>
 * Finer rules (e.g. only an OWNER manages OWNER accounts) live in the services.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] MANAGERS = {"OWNER", "ADMIN"};
    private static final String[] FINANCE_READERS = {"OWNER", "ADMIN", "VIEWER"};
    private static final String[] WRITERS = {"OWNER", "ADMIN", "STAFF"};

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // No cookie authenticates API calls (only the bearer token does); the refresh
                // cookie is SameSite=Strict and limited to /api/auth, so CSRF does not apply.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // CORS pre-flight
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/enums/**").permitAll()             // static breed/enum lists
                        .requestMatchers("/api/users/**").hasAnyRole(MANAGERS)
                        .requestMatchers(HttpMethod.GET, "/api/receipts/**", "/api/report/**").hasAnyRole(FINANCE_READERS)
                        .requestMatchers("/api/receipts/**", "/api/report/**").hasAnyRole(MANAGERS)
                        .requestMatchers("/api/me", "/api/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/**").hasAnyRole(WRITERS)
                        .anyRequest().authenticated()
                )
                // Same RFC 7807 body as every other API error, so the web app handles them alike.
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeProblem(res, HttpStatus.UNAUTHORIZED,
                                "Faça login para continuar."))
                        .accessDeniedHandler((req, res, ex) -> writeProblem(res, HttpStatus.FORBIDDEN,
                                "Você não tem permissão para esta ação.")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** JwtFilter is a @Component; keep Spring Boot from also registering it as a plain servlet filter. */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    private void writeProblem(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("requestId", RequestIdFilter.currentId());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
