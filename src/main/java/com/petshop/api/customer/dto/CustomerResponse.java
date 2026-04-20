package com.petshop.api.customer.dto;

import com.petshop.api.customer.domain.Pet;

import java.util.List;

public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String cpf,
        String email,
        List<Pet> pets
) {}
