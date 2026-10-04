package com.petshop.api.customer.dto;

import com.petshop.api.common.validation.Cpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String phone,
        @NotBlank @Cpf String cpf,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 255) String address,
        @Size(max = 1000) String obs
) {}
