package com.petshop.api.customer.dto;

import com.petshop.api.customer.domain.enums.CoatType;
import com.petshop.api.customer.domain.enums.SpecieType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRequest(
        @NotBlank @Size(max = 255) String name,
        @PastOrPresent LocalDate birthday,
        @PositiveOrZero double age,
        @NotNull SpecieType species,
        @NotBlank @Size(max = 255) String race,
        Boolean rabieVaccination,
        LocalDate rabieVaccinationDate,
        Boolean v10Vaccination,
        LocalDate v10VaccinationDate,
        Boolean dewormed,
        LocalDate dewormedDate,
        @Size(max = 255) String allergy,
        @Size(max = 255) String healthIssues,
        @PositiveOrZero Float weight,
        CoatType coatType,
        @Size(max = 1000) String observations,
        Long packId,
        @PositiveOrZero BigDecimal packagePrice
) {}
