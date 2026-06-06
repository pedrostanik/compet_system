package com.petshop.api.customer.dto;

import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.SpecieType;

import java.time.LocalDate;

public record PetRequest(
        String name,
        int age,
        SpecieType species,
        String race,
        Boolean rabieVaccination,
        LocalDate rabieVaccinationDate,
        Boolean v10Vaccination,
        LocalDate v10VaccinationDate,
        Boolean dewormed,
        LocalDate dewormedDate,
        String allergy,
        String healthIssues,
        Float weight,
        CoatType coatType,
        String observations
) {}
