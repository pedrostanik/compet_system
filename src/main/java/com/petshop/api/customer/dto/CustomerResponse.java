package com.petshop.api.customer.dto;

import java.util.List;

public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String cpf,
        String email,
        String address,
        List<PetResponse> pets
) {}
