package com.petshop.api.customer.dto;

public record PetRequest(
        String name,
        int age,
        String species,
        String race,
        String observations
) {}
