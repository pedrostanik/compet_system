package com.petshop.api.customer.dto;

import com.petshop.api.customer.domain.Customer;

public record PetResponse(
        long id,
        String name,
        int age,
        String species,
        String race,
        String observations,
        Customer customer
) {
}
