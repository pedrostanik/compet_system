package com.petshop.api.auth.controller;

import com.petshop.api.auth.dto.LoginRequest;
import com.petshop.api.auth.dto.LoginResponse;
import com.petshop.api.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (!request.username().equals(adminUsername) ||
                !request.password().equals(adminPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        return new LoginResponse(jwtService.generateToken(request.username()));
    }
}