package com.petshop.api.schedule.dto;

import java.time.LocalDateTime;

public record SchedulingResponse(
        Long id,
        Long customerId,
        String customerName,
        Long petId,
        String petName,
        String schedulingObservations,
        LocalDateTime time,
        Boolean scheduleHappened,
        Boolean isPackage
) {
}
