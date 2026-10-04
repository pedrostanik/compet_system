package com.petshop.api.packages.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PackRequest(
        @NotBlank @Size(max = 255) String name,
        // SchedulingService only knows how to expand these two frequencies into bookings.
        @NotNull @Pattern(regexp = "Semanal|Quinzenal", message = "deve ser Semanal ou Quinzenal") String frequencia,
        @NotEmpty List<@NotNull @Valid PackProtocolRequest> protocols
) {}
