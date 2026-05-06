package com.petshop.api.customer.dto;

import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.SpecieType;

import java.time.LocalDateTime;

public record PetResponse(
        long id,
        String name,
        int age,
        SpecieType species,
        String race,
        Boolean rabieVaccination,
        LocalDateTime rabieVaccinationDate,
        Boolean v10Vaccination,
        LocalDateTime v10VaccinationDate,
        Boolean dewormed,
        LocalDateTime dewormedDate,
        String allergy,
        String healthIssues,
        Float weight,
        CoatType coatType,
        String observations
) {
}
