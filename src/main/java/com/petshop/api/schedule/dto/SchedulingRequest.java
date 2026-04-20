package com.petshop.api.schedule.dto;

import java.time.LocalDateTime;

public record SchedulingRequest(
        Long customerId,
        String customerName,
        Long petId,
        String petName,
        String schedulingObservations,
        LocalDateTime time,
        Boolean isPackage
) {
}

