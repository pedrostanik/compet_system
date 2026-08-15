package com.petshop.api.customer.dto;

public record CustomerRequest(
        String name,
        String phone,
        String cpf,
        String email,
        String address,
        String obs
) {}
