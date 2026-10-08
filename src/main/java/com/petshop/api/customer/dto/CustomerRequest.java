package com.petshop.api.customer.dto;

import com.petshop.api.common.validation.Cpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String phone,
        @NotBlank @Cpf String cpf,
        // Optional and not format-checked for now: many customers have no e-mail,
        // and existing records hold free-text values.
        @Size(max = 255) String email,
        @Size(max = 255) String address,
        @Size(max = 1000) String obs
) {}
