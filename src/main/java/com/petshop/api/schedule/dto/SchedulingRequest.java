package com.petshop.api.schedule.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Past times are allowed on purpose: bookings are registered and edited after the fact.
public record SchedulingRequest(
        Long packId,
        @NotNull Long customerId,
        Integer packCycle,
        @Size(max = 255) String customerName,
        Long petId,
        @Size(max = 255) String petName,
        @Size(max = 255) String schedulingObservations,
        @NotNull LocalDateTime time,
        Boolean isPackage,
        @Positive Integer duration,
        List<@NotNull Long> protocolIds,
        @PositiveOrZero BigDecimal price
) {
}
